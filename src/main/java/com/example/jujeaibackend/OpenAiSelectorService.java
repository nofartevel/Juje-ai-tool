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
                1. packingChecklist: A list of categories, each with specific checklist items. Categories should be relevant to the trip type and child ages.
                2. forgottenItems: A list of practical reminders of things parents often forget.
                3. travelTips: Personalized advice based on the trip type, child ages, duration, and destination.
                4. recommendedProducts: A selection of relevant products from the provided Product Bank.
                5. travelResources: Future-looking placeholders for travel services like eSIM, insurance, or lounge access (return an empty list for now unless you have very generic helpful links).
                
                Return ONLY valid JSON in this exact shape:
                {
                  "packingChecklist": [
                    { "categoryName": "Category Name", "items": ["Item 1", "Item 2"] }
                  ],
                  "forgottenItems": ["Reminder 1", "Reminder 2"],
                  "travelTips": ["Tip 1", "Tip 2"],
                  "recommendedProducts": [ { "id": "p1", ... } ],
                  "travelResources": [ { "type": "eSIM", "name": "...", "description": "...", "link": "..." } ]
                }
                
                Be thorough, empathetic, and highly practical.
                """.formatted(contextJson, productsJson);

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(ChatModel.GPT_5_2)
                    .input(prompt)
                    .build();

            Response response = client.responses().create(params);
            String jsonText = extractJsonFromResponse(response);

            TravelPlan plan = objectMapper.readValue(jsonText, TravelPlan.class);
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

    public AiSelectorResult selectProducts(String userInput, String category) {
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

            String prompt = """
                You are helping choose products from a curated baby/toddler product bank.
            
                The user wrote a request.
                The user also selected a category.
                You must choose only from the provided products.
            
                Be strict about relevance, but do not artificially limit the list.
            
                A product should be selected if it would realistically belong in a useful curated list for that exact scenario.
                Do NOT include items that are only loosely related, weakly relevant, or generally useful for parenting without being a clear fit.
            
                For specific scenarios, prioritize scenario-specific usefulness over general usefulness.
            
                Each product may include:
                - section
                - keywords
                - use_cases
                - age_groups
                - needs
                - activity_type
            
                IMPORTANT:
                The selected category defines the primary direction of the request.
                Strongly prefer products that match the selected category.
            
                First, infer the main needs behind the user's request.
                Also consider the full real-life scenario, not just the most obvious part of it.
                Some requests may involve multiple phases or contexts, such as transit, stops, outdoor time, meals, cleanup, rest, or weather exposure.
            
                Also infer the main activity the user is trying to do.
            
                Then select all products that are clearly relevant to the full scenario.
            
                Selection rules:
                - Use ONLY product ids from the product list
                - Include all clearly relevant products
                - Do not leave out a strong product just because other similar products were also selected
                - It is okay to return a longer list if the products are genuinely useful
                - Do NOT add weak, borderline, or filler products just to make the list longer
                - If age is implied in the request, prefer products with matching age_groups
                - If most candidate products are generic rather than scenario-specific, do NOT return good
                - Prefer products that directly match what the user is most likely trying to achieve, not just anything that is technically relevant to the scenario
                - Avoid including products that belong to a different type of activity, even if they could be used in the same general environment
                - Prioritize products that match the selected category
                - Strongly prefer products with the same activity_type as the selected category
                - Avoid including products with a different activity_type unless they clearly add important value to the scenario
            
                Status rules:
                - good = the bank clearly covers the request with several genuinely relevant products
                - partial = only a few items are genuinely relevant, or coverage is incomplete
                - missing = there is no real coverage for this request
            
                Return ONLY valid JSON in this exact shape:
                {
                  "status": "good | partial | missing",
                  "message": "short user-facing message",
                  "selected_product_ids": ["id1", "id2"]
                }
            
                User category:
                "%s"
            
                User request:
                "%s"
            
                Product bank:
                %s
                """.formatted(category, userInput, productsJson);

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(ChatModel.GPT_5_2)
                    .input(prompt)
                    .build();

            Response response = client.responses().create(params);
            String jsonText = extractJsonFromResponse(response);

            AiSelectorResult result = objectMapper.readValue(jsonText, AiSelectorResult.class);
            normalizeResult(result);
            return result;

        } catch (Exception e) {
            e.printStackTrace();

            AiSelectorResult fallback = new AiSelectorResult();
            fallback.setStatus("missing");
            fallback.setMessage("I couldn’t confidently match products for this request right now.");
            fallback.setSelectedProductIds(List.of());
            return fallback;
        }
    }

    private void normalizeResult(AiSelectorResult result) {
        if (result.getSelectedProductIds() == null) {
            result.setSelectedProductIds(List.of());
        }

        if ("good".equalsIgnoreCase(result.getStatus()) && result.getSelectedProductIds().size() < 2) {
            result.setStatus("partial");
        }
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