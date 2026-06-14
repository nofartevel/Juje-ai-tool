package com.example.jujeaibackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiSelectorService {

    private final OpenAIClient client;
    private final ObjectMapper objectMapper;
    private final ProductService productService;

    public OpenAiSelectorService(ProductService productService) {
        this.client = OpenAIOkHttpClient.fromEnv();
        this.objectMapper = new ObjectMapper();
        this.productService = productService;
    }

    public TravelPlan generateTravelPlan(TripContext context) {
        try {
            List<Product> products = productService.getAllProducts();

            String productsJson = objectMapper.writeValueAsString(
                    products.stream().map(p -> {
                        Map<String, Object> productMap = new java.util.HashMap<>();
                        productMap.put("id", p.getId());
                        productMap.put("name", p.getName());
                        productMap.put("section", p.getSection());
                        productMap.put("why", p.getWhy());
                        productMap.put("keywords", p.getKeywords());
                        productMap.put("use_cases", p.getUse_cases());
                        productMap.put("age_groups", p.getAge_groups() == null ? List.of() : p.getAge_groups());
                        productMap.put("needs", p.getNeeds() == null ? List.of() : p.getNeeds());
                        productMap.put("activity_type", p.getActivity_type());
                        return productMap;
                    }).toList()
            );

            String contextJson = objectMapper.writeValueAsString(context);

            String prompt = """
                You are an Expert Family Travel Planner.
                Your goal is to help parents prepare for a trip with their children.
                
                User Trip Context:
                %s
                
                Available Product Bank:
                %s
                
                You must generate a complete travel preparation plan in JSON format.
                
                CRITICAL INSTRUCTIONS:
                1. Trip Type is the PRIMARY driver. A FLIGHT, BEACH, or ROAD_TRIP requires fundamentally different items and advice.
                2. Use child ages (in months) to provide highly specific advice. An 8-month-old's needs are very different from a 24-month-old's.
                3. If a destination is provided, include specific preparation tips for that location (e.g., climate, terrain, local availability of baby supplies).
                
                The plan should include:
                1. packingChecklist: A list of categories, each with specific checklist items. Categories should be relevant to the trip type and child ages. Use multiple categories to keep the checklist organized.
                2. forgottenItems: Top 5 Things Parents Forget. Limit to EXACTLY 5 high-value, practical reminders.
                3. travelTips: Quick Travel Tips. Limit to EXACTLY 5 concise tips (1-2 short sentences each). Highly actionable and easy to scan.
                4. recommendedProducts: A selection of relevant products from the provided Product Bank.
                5. travelResources: Future-looking placeholders for travel services like eSIM, insurance, or lounge access (return an empty list for now unless you have very generic helpful links).
                
                Return ONLY valid JSON in this exact shape:
                {
                  "packingChecklist": [
                    { "categoryName": "Category Name", "items": ["Item 1", "Item 2"] }
                  ],
                  "forgottenItems": ["Reminder 1", "Reminder 2", "Reminder 3", "Reminder 4", "Reminder 5"],
                  "travelTips": ["Tip 1", "Tip 2", "Tip 3", "Tip 4", "Tip 5"],
                  "recommendedProducts": [ { "id": "p1", ... } ],
                  "travelResources": [ { "type": "eSIM", "name": "...", "description": "...", "link": "..." } ]
                }
                
                Be thorough in the checklist, but concise and high-impact in tips and reminders.
                """.formatted(contextJson, productsJson);

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(ChatModel.GPT_5_2)
                    .input(prompt)
                    .build();

            Response response = client.responses().create(params);
            String jsonText = extractJsonFromResponse(response);

            TravelPlan plan = objectMapper.readValue(jsonText, TravelPlan.class);
            
            // Enrich recommended products with full data from ProductService
            if (plan.getRecommendedProducts() != null) {
                List<Product> enriched = plan.getRecommendedProducts().stream()
                        .map(p -> productService.getProductById(p.getId()))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                plan.setRecommendedProducts(enriched);
            }

            plan.setContext(context);
            return plan;

        } catch (Exception e) {
            e.printStackTrace();
            return createFallbackPlan(context);
        }
    }

    private TravelPlan createFallbackPlan(TripContext context) {
        TravelPlan fallback = new TravelPlan();
        fallback.setForgottenItems(List.of("Always bring an extra change of clothes for the kids."));
        fallback.setTravelTips(List.of("Try to maintain a similar routine to home as much as possible."));
        fallback.setPackingChecklist(List.of(new ChecklistCategory("Essentials", List.of("Passports", "First aid kit"))));
        fallback.setRecommendedProducts(List.of());
        fallback.setTravelResources(List.of());
        fallback.setContext(context);
        return fallback;
    }


    private String extractJsonFromResponse(Response response) {
        String raw = response.toString();

        String marker = "outputText=ResponseOutputText{annotations=[], text=";
        int start = raw.indexOf(marker);
        if (start == -1) {
            throw new RuntimeException("Could not find AI text in response");
        }

        start += marker.length();
        int end = raw.indexOf(", type=output_text", start);
        if (end == -1) {
            throw new RuntimeException("Could not find end of AI text in response");
        }

        return raw.substring(start, end).trim();
    }
}