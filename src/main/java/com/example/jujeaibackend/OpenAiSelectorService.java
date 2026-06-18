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
            new ChecklistCategory("Diapering & Hygiene", List.of("Diapers (1 for every 2 hours of travel)", "Wet Wipes", "Changing Pad", "Diaper Disposal Bags", "Hand Sanitizer"))
        ),
        TripType.ROAD_TRIP, List.of(
            new ChecklistCategory("Car Essentials", List.of("Car Seat", "Window Shades", "Emergency Kit", "Trash Bags", "Paper Towels")),
            new ChecklistCategory("Diapering & Hygiene", List.of("Diapers", "Wet Wipes", "Changing Pad", "Hand Sanitizer"))
        ),
        TripType.BEACH, List.of(
            new ChecklistCategory("Sun & Water Gear", List.of("Sunscreen", "Swim Diapers", "Sun Hats", "UV Protection Swimwear", "Sunglasses"))
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
            List<Product> allProducts = productService.getAllProducts();

            // OPTIMIZATION: Pre-filter products based on trip type to reduce prompt size
            String tripTypeStr = context.getTripType().name();
            List<Product> filteredProducts = allProducts.stream()
                    .filter(p -> p.getActivity_type() == null || 
                                 p.getActivity_type().equalsIgnoreCase("GENERAL") || 
                                 p.getActivity_type().equalsIgnoreCase(tripTypeStr))
                    .toList();

            // OPTIMIZATION: Send only essential product metadata
            String productsJson = objectMapper.writeValueAsString(
                    filteredProducts.stream().map(p -> {
                        Map<String, Object> productMap = new java.util.HashMap<>();
                        productMap.put("id", p.getId());
                        productMap.put("name", p.getName());
                        productMap.put("keywords", p.getKeywords());
                        productMap.put("why", p.getWhy());
                        productMap.put("age_groups", p.getAge_groups());
                        productMap.put("activity_type", p.getActivity_type());
                        return productMap;
                    }).toList()
            );

            String contextJson = objectMapper.writeValueAsString(context);

            String prompt = """
                You are an Expert Family Travel Planner.
                Your goal is to help parents prepare for a trip with their children by providing a HIGHLY PERSONALIZED travel plan.
                
                User Trip Context:
                %s
                
                Available Product Bank (Metadata):
                %s
                
                You must generate a complete travel preparation plan in JSON format.
                
                PERSONALIZATION IS THE TOP PRIORITY:
                1. Reference child ages (in months) throughout the tips, reminders, and checklist. An 8-month-old, 18-month-old, and 4-year-old have very different needs.
                2. Trip Type is the PRIMARY driver. A FLIGHT, BEACH, or ROAD_TRIP requires fundamentally different items and advice.
                3. Account for weather conditions (HOT/COLD/MIXED).
                
                CONTENT GUIDELINES:
                - Instead of "Bring snacks", use "For your [Age]-month-old, bring [Specific Tip] for the [Trip Type]".
                - Instead of "Pack layers", use "Since it's a [Weather] trip, pack [Specific Clothing Item] for [Child Age]-month-old".
                - Focus on specialized advice that a generic search engine wouldn't provide.
                
                OUTPUT REQUIREMENTS:
                - packingChecklist: Detailed categories and items. Focus on age-specific gear, clothing layers for the weather, and feeding/sleep needs. Use highly descriptive item names.
                - forgottenItems: Exactly 5 high-value, practical reminders specific to this exact trip scenario and child ages.
                - travelTips: Exactly 5 highly personalized tips. Be specific, actionable, and reference the child's age and trip circumstances.
                - recommendedProducts: Select at least 6-10 of the MOST RELEVANT products from the Product Bank (if available).
                
                DO NOT include basic essentials like passports or diapers (these are added automatically).
                Focus on the "Value-Add" items that make the trip smoother.
                
                Return ONLY valid JSON in this exact shape:
                {
                  "packingChecklist": [
                    { "categoryName": "Category Name", "items": ["Item 1", "Item 2"] }
                  ],
                  "forgottenItems": ["Reminder 1", "Reminder 2", "Reminder 3", "Reminder 4", "Reminder 5"],
                  "travelTips": ["Tip 1", "Tip 2", "Tip 3", "Tip 4", "Tip 5"],
                  "recommendedProducts": [ { "id": "product_id", "why": "short explanation" } ],
                  "travelResources": []
                }
                """.formatted(contextJson, productsJson);

            long promptTime = System.currentTimeMillis();
            System.out.println("[PERF] Prompt Building: " + (promptTime - startTime) + "ms");

            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(ChatModel.GPT_4O)
                    .input(prompt)
                    .build();

            Response response = client.responses().create(params);
            long apiTime = System.currentTimeMillis();
            System.out.println("[PERF] OpenAI API Response: " + (apiTime - promptTime) + "ms");

            String jsonText = extractJsonFromResponse(response);
            System.out.println("[DEBUG] Raw AI JSON: " + jsonText);

            TravelPlan plan = objectMapper.readValue(jsonText, TravelPlan.class);
            if (plan.getRecommendedProducts() != null) {
                System.out.println("[DEBUG] AI Selected Product IDs: " + 
                    plan.getRecommendedProducts().stream().map(Product::getId).toList());
            } else {
                System.out.println("[DEBUG] AI Selected ZERO products.");
            }
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

            System.out.println("[DEBUG] Catalog Total Products: " + allProducts.size());
            System.out.println("[DEBUG] Filtered Products (TripType=" + tripTypeStr + "): " + filteredProducts.size());

            // Enrich recommended products and apply fallback if needed
            List<Product> recommended = new java.util.ArrayList<>();
            if (plan.getRecommendedProducts() != null) {
                recommended.addAll(plan.getRecommendedProducts().stream()
                        .map(p -> productService.getProductById(p.getId()))
                        .filter(java.util.Objects::nonNull)
                        .toList());
            }
            System.out.println("[DEBUG] Products selected after mapping IDs: " + recommended.size());

            // Fallback for products: ensure at least 6 products if relevant ones exist
            if (recommended.size() < 6) {
                System.out.println("[DEBUG] Applying product fallback. Current size: " + recommended.size());
                
                // Collect existing IDs to avoid duplicates
                java.util.Set<String> existingIds = recommended.stream()
                        .map(Product::getId)
                        .collect(java.util.stream.Collectors.toSet());

                List<Product> fallbackProducts = allProducts.stream()
                        .filter(p -> !existingIds.contains(p.getId()))
                        .filter(p -> {
                            // Match by trip type (activity_type)
                            boolean matchesTrip = p.getActivity_type() != null && 
                                                 (p.getActivity_type().equalsIgnoreCase("GENERAL") || 
                                                  p.getActivity_type().equalsIgnoreCase(tripTypeStr) ||
                                                  (tripTypeStr.equals("FLIGHT") && p.getActivity_type().equalsIgnoreCase("TRAVEL")) ||
                                                  (tripTypeStr.equals("ROAD_TRIP") && p.getActivity_type().equalsIgnoreCase("TRAVEL")) ||
                                                  (tripTypeStr.equals("FLIGHT") && p.getActivity_type().equalsIgnoreCase("PLAY")) ||
                                                  (tripTypeStr.equals("ROAD_TRIP") && p.getActivity_type().equalsIgnoreCase("PLAY")));
                            
                            return matchesTrip;
                        })
                        .limit(8 - recommended.size())
                        .toList();
                
                System.out.println("[DEBUG] Adding " + fallbackProducts.size() + " fallback products");
                recommended.addAll(fallbackProducts);
            }
            plan.setRecommendedProducts(recommended);

            System.out.println("[PERF] Total Backend Time: " + (System.currentTimeMillis() - startTime) + "ms");
            
            // Log recommended products for debugging
            if (plan.getRecommendedProducts() != null) {
                System.out.println("Recommended Products Size: " + plan.getRecommendedProducts().size());
                plan.getRecommendedProducts().forEach(p -> 
                    System.out.println("Product: ID=" + p.getId() + ", Name=" + p.getName() + ", Image=" + p.getImage())
                );
            } else {
                System.out.println("Recommended Products is NULL");
            }

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

        String jsonText = raw.substring(start, end).trim();

        // Handle cases where the AI wraps JSON in Markdown code blocks
        if (jsonText.startsWith("```")) {
            // Remove starting block (e.g., ```json or just ```)
            int firstNewline = jsonText.indexOf("\n");
            if (firstNewline != -1) {
                jsonText = jsonText.substring(firstNewline).trim();
            } else {
                jsonText = jsonText.substring(3).trim();
            }
            
            // Remove ending block
            if (jsonText.endsWith("```")) {
                jsonText = jsonText.substring(0, jsonText.length() - 3).trim();
            }
        }

        return jsonText;
    }
}