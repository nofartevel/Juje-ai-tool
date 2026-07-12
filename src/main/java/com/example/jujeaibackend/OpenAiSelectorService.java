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
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey != null && !apiKey.isBlank()) {
            this.client = OpenAIOkHttpClient.fromEnv();
        } else {
            this.client = null;
        }
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

            // Include all products that match the trip context
            String weatherStr = context.getWeather() != null ? context.getWeather().name().toLowerCase() : "";
            List<Integer> childAges = context.getChildren() != null ? context.getChildren().stream().map(ChildDetail::getAgeMonths).toList() : List.of();
            String transportStr = context.getTransportType() != null ? context.getTransportType().name().toLowerCase() : "";
            List<String> destinationTypes = context.getDestinationTypes() != null 
                    ? context.getDestinationTypes().stream().map(t -> t.name().toLowerCase()).toList() 
                    : List.of();

            List<String> removedByFilter = new java.util.ArrayList<>();
            List<String> inclusionReasons = new java.util.ArrayList<>();

            List<Product> filteredProducts = allProducts.stream()
                    .filter(p -> {
                        StringBuilder reason = new StringBuilder();
                        reason.append("Product: ").append(p.getId()).append("\nReason:\n");

                        // 1. Context Matching based on product_scope
                        String scope = p.getProduct_scope() != null ? p.getProduct_scope() : "universal";
                        boolean matchesContext = false;
                        
                        if ("universal".equalsIgnoreCase(scope)) {
                            // Universal products match if they are NOT destination-specific OR they match at least one selected destination
                            boolean hasDestinations = p.getDestination_types() != null && !p.getDestination_types().isEmpty();
                            boolean matchesDestination = !hasDestinations || destinationTypes.stream().anyMatch(p.getDestination_types()::contains);

                            // AND they are NOT transport-specific OR they match the transport
                            boolean hasTransports = p.getTransport_types() != null && !p.getTransport_types().isEmpty();
                            boolean matchesTransport = !hasTransports || p.getTransport_types().contains(transportStr);
                            
                            matchesContext = matchesDestination && matchesTransport;
                        } else if ("transport_specific".equalsIgnoreCase(scope)) {
                            matchesContext = p.getTransport_types() != null && p.getTransport_types().contains(transportStr);
                        } else if ("destination_specific".equalsIgnoreCase(scope)) {
                            // Intersection check
                            matchesContext = p.getDestination_types() != null && destinationTypes.stream().anyMatch(p.getDestination_types()::contains);
                        }

                        if (!matchesContext) {
                            removedByFilter.add(p.getId() + ": context mismatch (Scope: " + scope + ")");
                            return false;
                        }

                        // 4. Age Group Matching
                        boolean matchesAge = true;
                        if (p.getAge_groups() != null && !p.getAge_groups().isEmpty() && !childAges.isEmpty()) {
                            matchesAge = false;
                            for (Integer ageMonths : childAges) {
                                List<String> groups = getAgeGroups(ageMonths);
                                if (p.getAge_groups().stream().anyMatch(groups::contains)) {
                                    matchesAge = true;
                                    break;
                                }
                            }
                        }
                        if (!matchesAge) {
                            removedByFilter.add(p.getId() + ": age mismatch");
                            return false;
                        }

                        // 5. Weather Matching
                        boolean matchesWeather = p.getWeather() == null || p.getWeather().isEmpty() || p.getWeather().stream().anyMatch(w -> w.equalsIgnoreCase(weatherStr));
                        if (!matchesWeather) {
                            removedByFilter.add(p.getId() + ": weather mismatch");
                            return false;
                        }

                        return true;
                    }).toList();

            // Sort products: 
            // 1. Matches destination + transport
            // 2. Matches destination
            // 3. Universal product (no destination specified in metadata)
            // 4. Transport-only product
            List<Product> sortedProducts = filteredProducts.stream()
                    .sorted((p1, p2) -> {
                        // Scoring
                        int score1 = calculateProductScore(p1, transportStr, destinationTypes);
                        int score2 = calculateProductScore(p2, transportStr, destinationTypes);
                        
                        if (score1 != score2) {
                            return score2 - score1; // Higher score first
                        }

                        // Tie-break with is_essential
                        if (p1.is_essential() != p2.is_essential()) {
                            return p2.is_essential() ? 1 : -1;
                        }
                        return 0;
                    })
                    .toList();

            // OPTIMIZATION: Send only essential product metadata
            String productsJson = objectMapper.writeValueAsString(
                    sortedProducts.stream().map(p -> {
                        Map<String, Object> productMap = new java.util.HashMap<>();
                        productMap.put("id", p.getId());
                        productMap.put("name", p.getName());
                        productMap.put("keywords", p.getKeywords());
                        productMap.put("why", p.getWhy());
                        productMap.put("age_groups", p.getAge_groups() == null ? List.of() : p.getAge_groups());
                        productMap.put("activity_type", p.getActivity_type());
                        productMap.put("trip_types", p.getTrip_types() == null ? List.of() : p.getTrip_types());
                        productMap.put("transport_types", p.getTransport_types() == null ? List.of() : p.getTransport_types());
                        productMap.put("destination_types", p.getDestination_types() == null ? List.of() : p.getDestination_types());
                        productMap.put("weather", p.getWeather() == null ? List.of() : p.getWeather());
                        productMap.put("product_scope", p.getProduct_scope());
                        productMap.put("is_essential", p.is_essential());
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
                2. Transport Type and Destination Types are the PRIMARY drivers. User selected %d destination styles. Combine their needs (e.g. if Beach and City are selected, address both).
                3. Account for weather conditions (HOT/COLD/MIXED).
                
                CONTENT GUIDELINES:
                - Instead of "Bring snacks", use "For your [Age]-month-old, bring [Specific Tip] for the [Trip Type]".
                - Instead of "Pack layers", use "Since it's a [Weather] trip, pack [Specific Clothing Item] for [Child Age]-month-old".
                - Focus on specialized advice that a generic search engine wouldn't provide.
                
                OUTPUT REQUIREMENTS:
                - packingChecklist: Detailed categories and items. Focus on age-specific gear, clothing layers for the weather, and feeding/sleep needs.
                - IMPORTANT: Use SHORT, SCANNABLE item names (e.g., "Lightweight outfits" instead of "Lightweight breathable cotton outfits for 24-month-old").
                - forgottenItems: Exactly 5 high-value, practical reminders specific to this exact trip scenario and child ages.
                - travelTips: Exactly 5 highly personalized tips. Be specific, actionable, and reference the child's age and trip circumstances.
                
                DO NOT include basic essentials like passports or diapers (these are added automatically).
                Focus on the "Value-Add" items that make the trip smoother.
                
                Return ONLY valid JSON in this exact shape:
                {
                  "packingChecklist": [
                    { "categoryName": "Category Name", "items": ["Item 1", "Item 2"] }
                  ],
                  "forgottenItems": ["Reminder 1", "Reminder 2", "Reminder 3", "Reminder 4", "Reminder 5"],
                  "travelTips": ["Tip 1", "Tip 2", "Tip 3", "Tip 4", "Tip 5"],
                  "travelResources": []
                }
                """.formatted(contextJson, productsJson, destinationTypes.size());

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
            System.out.println("[DEBUG] Filtered Products (Transport=" + transportStr + ", Destinations=" + destinationTypes + ", Weather=" + weatherStr + "): " + filteredProducts.size());

            // Use backend-filtered and sorted products directly
            plan.setRecommendedProducts(sortedProducts);
            plan.setContext(context);

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


    private int calculateProductScore(Product p, String transportStr, List<String> destinationTypes) {
        boolean matchesDest = p.getDestination_types() != null && destinationTypes.stream().anyMatch(p.getDestination_types()::contains);
        boolean matchesTransport = p.getTransport_types() != null && p.getTransport_types().contains(transportStr);
        boolean hasDestMetadata = p.getDestination_types() != null && !p.getDestination_types().isEmpty();
        
        if (matchesDest && matchesTransport) return 4;
        if (matchesDest) return 3;
        if (!hasDestMetadata) return 2; // Universal
        if (matchesTransport) return 1;
        return 0;
    }

    private List<String> getAgeGroups(int ageMonths) {
        List<String> groups = new java.util.ArrayList<>();
        if (ageMonths <= 6) groups.add("0m-6m");
        if (ageMonths >= 6 && ageMonths <= 12) groups.add("6m-12m");
        if (ageMonths >= 12 && ageMonths <= 24) groups.add("1y-2y");
        if (ageMonths >= 24 && ageMonths <= 48) groups.add("2y-4y");
        if (ageMonths >= 48 && ageMonths <= 84) groups.add("4y-7y");
        if (ageMonths >= 84) groups.add("7y+");
        return groups;
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