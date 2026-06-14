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

    // Static checklist templates to reduce AI output size
    private static final Map<TripType, List<ChecklistCategory>> STATIC_TEMPLATES = Map.of(
        TripType.FLIGHT, List.of(
            new ChecklistCategory("Flight Day Essentials", List.of("Passports & Documents", "Boarding Passes", "Wallet & Cash", "Phone & Chargers")),
            new ChecklistCategory("Diapering & Hygiene", List.of("Diapers (1 for every 2 hours of travel)", "Wet Wipes", "Changing Pad", "Diaper Disposal Bags", "Hand Sanitizer")),
            new ChecklistCategory("Health & Safety", List.of("First Aid Kit", "Children's Pain Reliever", "Prescription Medications", "Thermometer", "Antibacterial Wipes"))
        ),
        TripType.ROAD_TRIP, List.of(
            new ChecklistCategory("Car Essentials", List.of("Car Seat", "Window Shades", "Emergency Kit", "Trash Bags", "Paper Towels")),
            new ChecklistCategory("Diapering & Hygiene", List.of("Diapers", "Wet Wipes", "Changing Pad", "Hand Sanitizer")),
            new ChecklistCategory("Health & Safety", List.of("First Aid Kit", "Motion Sickness Bags", "Hand Sanitizer"))
        ),
        TripType.BEACH, List.of(
            new ChecklistCategory("Sun & Water Gear", List.of("Sunscreen", "Swim Diapers", "Sun Hats", "UV Protection Swimwear", "Sunglasses")),
            new ChecklistCategory("Health & Safety", List.of("First Aid Kit", "Aloe Vera", "Bug Spray", "Hand Sanitizer"))
        )
    );

    public OpenAiSelectorService(ProductService productService) {
        this.client = OpenAIOkHttpClient.fromEnv();
        this.objectMapper = new ObjectMapper();
        this.productService = productService;
    }

    public TravelPlan generateTravelPlan(TripContext context) {
        return generateTravelPlanWithRetry(context, 3);
    }

    private TravelPlan generateTravelPlanWithRetry(TripContext context, int retries) {
        long startTime = System.currentTimeMillis();
        try {
            List<Product> products = productService.getAllProducts();

            // OPTIMIZATION: Send only essential product metadata to reduce prompt size
            String productsJson = objectMapper.writeValueAsString(
                    products.stream().map(p -> {
                        Map<String, Object> productMap = new java.util.HashMap<>();
                        productMap.put("id", p.getId());
                        productMap.put("name", p.getName());
                        productMap.put("section", p.getSection());
                        productMap.put("keywords", p.getKeywords());
                        productMap.put("use_cases", p.getUse_cases());
                        return productMap;
                    }).toList()
            );

            String contextJson = objectMapper.writeValueAsString(context);

            String prompt = """
                You are an Expert Family Travel Planner.
                Your goal is to help parents prepare for a trip with their children.
                
                User Trip Context:
                %s
                
                Available Product Bank (Metadata):
                %s
                
                You must generate a complete travel preparation plan in JSON format.
                
                CRITICAL INSTRUCTIONS:
                1. Trip Type is the PRIMARY driver. A FLIGHT, BEACH, or ROAD_TRIP requires fundamentally different items and advice.
                2. Use child ages (in months) to provide highly specific advice. An 8-month-old's needs are very different from a 24-month-old's.
                3. If a destination is provided, include specific preparation tips for that location (e.g., climate, terrain, local availability of baby supplies).
                
                The plan should include:
                1. packingChecklist: A list of ONLY the MOST IMPORTANT DYNAMIC items tailored to the child's age and specific trip details.
                   Keep categories to a maximum of 3. Keep items per category to a maximum of 5.
                   DO NOT include basic essentials like passports, diapers, or sunscreen (these are added automatically).
                   Focus on: Age-specific toys, specific clothing layers for the destination/weather, and feeding supplies.
                   Return categories like: "Age-Specific Entertainment", "Feeding & Comfort", "Specific Gear for [Destination]".
                2. forgottenItems: Top 5 Things Parents Forget. Limit to EXACTLY 5 high-value, practical reminders.
                3. travelTips: Quick Travel Tips. Limit to EXACTLY 5 concise tips (1-2 short sentences each).
                4. recommendedProducts: A selection of relevant products from the provided Product Bank. Return ONLY the ID.
                5. travelResources: Return an empty list.
                
                Return ONLY valid JSON in this exact shape:
                {
                  "packingChecklist": [
                    { "categoryName": "Category Name", "items": ["Item 1", "Item 2"] }
                  ],
                  "forgottenItems": ["Reminder 1", "Reminder 2", "Reminder 3", "Reminder 4", "Reminder 5"],
                  "travelTips": ["Tip 1", "Tip 2", "Tip 3", "Tip 4", "Tip 5"],
                  "recommendedProducts": [ { "id": "p1" } ],
                  "travelResources": []
                }
                
                Be thorough in the checklist, but concise and high-impact in tips and reminders.
                """.formatted(contextJson, productsJson);

            long promptTime = System.currentTimeMillis();
            System.out.println("[PERF] Prompt Building: " + (promptTime - startTime) + "ms");

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(ChatModel.GPT_4O_MINI)
                    .input(prompt)
                    .build();

            Response response = client.responses().create(params);
            long apiTime = System.currentTimeMillis();
            System.out.println("[PERF] OpenAI API Response: " + (apiTime - promptTime) + "ms");

            String jsonText = extractJsonFromResponse(response);

            TravelPlan plan = objectMapper.readValue(jsonText, TravelPlan.class);
            long parseTime = System.currentTimeMillis();
            System.out.println("[PERF] JSON Parsing: " + (parseTime - apiTime) + "ms");
            
            // OPTIMIZATION: Merge with static templates to reduce AI generation time
            List<ChecklistCategory> dynamicChecklist = plan.getPackingChecklist();
            List<ChecklistCategory> fullChecklist = new java.util.ArrayList<>();
            if (STATIC_TEMPLATES.containsKey(context.getTripType())) {
                fullChecklist.addAll(STATIC_TEMPLATES.get(context.getTripType()));
            }
            if (dynamicChecklist != null) {
                fullChecklist.addAll(dynamicChecklist);
            }
            plan.setPackingChecklist(fullChecklist);

            // Enrich recommended products with full data from ProductService
            if (plan.getRecommendedProducts() != null) {
                List<Product> enriched = plan.getRecommendedProducts().stream()
                        .map(p -> productService.getProductById(p.getId()))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                plan.setRecommendedProducts(enriched);
            }

            System.out.println("[PERF] Total Backend Time: " + (System.currentTimeMillis() - startTime) + "ms");
            return plan;

        } catch (com.openai.errors.RateLimitException e) {
            System.err.println("OpenAI Rate Limit/Quota Exceeded: " + e.getMessage());
            // If it's likely a quota/billing issue (which often returns 429), fail immediately
            // Temporary rate limits might also return 429, but per requirements we treat quota as immediate failure.
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return createFallbackPlan(context);
        }
    }

    private TravelPlan createFallbackPlan(TripContext context) {
        System.out.println("Creating fallback plan for context: " + context.getTripType());
        TravelPlan fallback = new TravelPlan();
        fallback.setForgottenItems(List.of("Always bring an extra change of clothes for the kids."));
        fallback.setTravelTips(List.of("Try to maintain a similar routine to home as much as possible."));
        
        List<ChecklistCategory> checklist = new java.util.ArrayList<>();
        if (STATIC_TEMPLATES.containsKey(context.getTripType())) {
            checklist.addAll(STATIC_TEMPLATES.get(context.getTripType()));
        } else {
            checklist.add(new ChecklistCategory("Essentials", List.of("Passports", "First aid kit")));
        }
        fallback.setPackingChecklist(checklist);
        
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