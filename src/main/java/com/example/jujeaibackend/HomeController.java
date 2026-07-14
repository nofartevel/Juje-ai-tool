package com.example.jujeaibackend;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import java.util.*;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class HomeController {

    private final ProductService productService;
    private final SessionService sessionService;
    private final OpenAiSelectorService openAiSelectorService;
    private final AnalyticsService analyticsService;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    public HomeController(ProductService productService,
                          SessionService sessionService,
                          OpenAiSelectorService openAiSelectorService,
                          AnalyticsService analyticsService) {
        this.sessionService = sessionService;
        this.productService = productService;
        this.openAiSelectorService = openAiSelectorService;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/api/v1/analytics/session-start")
    public ResponseEntity<?> sessionStart(@RequestBody Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");
        Map<String, Object> contextMap = (Map<String, Object>) payload.get("context");
        
        TripContext context = null;
        if (contextMap != null) {
            context = mapTripContext(contextMap);
        }
        
        analyticsService.startSession(sessionId, context);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/sessions")
    public List<SessionAnalytics> getSessions() {
        return analyticsService.getAllSessions();
    }

    @PostMapping("/api/v1/analytics/plan-generated")
    public ResponseEntity<?> planGenerated(@RequestBody Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");
        boolean success = (boolean) payload.get("success");
        String errorMessage = (String) payload.get("errorMessage");
        List<Map<String, String>> productsRaw = (List<Map<String, String>>) payload.get("products");
        List<String> categories = (List<String>) payload.get("categories");

        List<SessionAnalytics.ProductInfo> products = new ArrayList<>();
        if (productsRaw != null) {
            for (Map<String, String> p : productsRaw) {
                products.add(new SessionAnalytics.ProductInfo(p.get("productId"), p.get("productName")));
            }
        }

        analyticsService.planGenerated(sessionId, success, errorMessage, products, categories);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/analytics/product-click")
    public ResponseEntity<?> productClick(@RequestBody Map<String, String> payload) {
        analyticsService.productClick(payload.get("sessionId"), payload.get("productId"), payload.get("productName"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/analytics/print-click")
    public ResponseEntity<?> printClick(@RequestBody Map<String, String> payload) {
        analyticsService.printClick(payload.get("sessionId"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/analytics/share-click")
    public ResponseEntity<?> shareClick(@RequestBody Map<String, String> payload) {
        analyticsService.shareClick(payload.get("sessionId"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/analytics/track-step")
    public ResponseEntity<?> trackStep(@RequestBody Map<String, Object> payload) {
        String sessionId = (String) payload.get("sessionId");
        String stepName = (String) payload.get("stepName");
        Map<String, Object> contextMap = (Map<String, Object>) payload.get("context");

        TripContext context = null;
        if (contextMap != null) {
            context = mapTripContext(contextMap);
        }

        analyticsService.trackStep(sessionId, stepName, context);
        return ResponseEntity.ok().build();
    }

    private TripContext mapTripContext(Map<String, Object> contextMap) {
        TripContext context = new TripContext();
        try {
            if (contextMap.get("tripType") != null) {
                context.setTripType(TripType.valueOf((String) contextMap.get("tripType")));
            }
            if (contextMap.get("transportType") != null) {
                context.setTransportType(TransportType.valueOf((String) contextMap.get("transportType")));
            }
            if (contextMap.get("destinationTypes") != null) {
                List<String> types = (List<String>) contextMap.get("destinationTypes");
                context.setDestinationTypes(types.stream()
                        .map(DestinationType::valueOf)
                        .toList());
            }
            if (contextMap.get("weather") != null) {
                context.setWeather(WeatherType.valueOf((String) contextMap.get("weather")));
            }

            if (contextMap.get("children") != null) {
                List<ChildDetail> children = new ArrayList<>();
                for (Map<String, Object> c : (List<Map<String, Object>>) contextMap.get("children")) {
                    Object ageVal = c.get("ageInMonths");
                    if (ageVal == null) ageVal = c.get("ageMonths");

                    if (ageVal instanceof Integer) {
                        children.add(new ChildDetail((Integer) ageVal));
                    } else if (ageVal instanceof Long) {
                        children.add(new ChildDetail(((Long) ageVal).intValue()));
                    }
                }
                context.setChildren(children);
            }
        } catch (Exception e) {
            System.err.println("Error mapping TripContext: " + e.getMessage());
        }
        return context;
    }

    @PostMapping("/api/v1/generate-travel-plan")
    public ResponseEntity<?> generateTravelPlan(@RequestBody TripContext context) {
        try {
            return ResponseEntity.ok(openAiSelectorService.generateTravelPlan(context));
        } catch (com.openai.errors.RateLimitException e) {
            return ResponseEntity.status(429).body(Map.of(
                "error", "Service Unavailable",
                "message", "The travel planner is temporarily unavailable. Please try again later."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Generation Failed",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/api/v1/save-plan")
    public ResponseEntity<?> savePlan(@RequestBody TravelPlan plan) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            try {
                System.out.println("Plan payload: " + mapper.writeValueAsString(plan));
            } catch (Exception e) {
                System.err.println("Could not log plan payload: " + e.getMessage());
            }
            
            if (plan != null) {
                // Log the payload details for debugging
                if (plan.getRecommendedProducts() != null) {
                    for (Product p : plan.getRecommendedProducts()) {
                        if (p.getIs_essential() == null) {
                            System.out.println("DEBUG: Product " + (p.getId() != null ? p.getId() : "unknown") + " has NULL is_essential");
                        }
                    }
                }
                System.out.println("TravelPlan found: true");
                TripContext context = plan.getContext();
                if (context != null) {
                    System.out.println("Context presence: true");
                    System.out.println("tripType: " + context.getTripType());
                    System.out.println("children: " + (context.getChildren() != null ? context.getChildren().size() : 0));
                    System.out.println("weather: " + context.getWeather());
                } else {
                    System.out.println("Context presence: false");
                }
                List<ChecklistCategory> checklist = plan.getPackingChecklist();
                if (checklist != null) {
                    System.out.println("packingChecklist size: " + checklist.size());
                    System.out.println("Raw checklist items: " + checklist);
                    for (ChecklistCategory cat : checklist) {
                        if (cat == null) {
                            System.out.println("Skipping null checklist category");
                            continue;
                        }
                        int itemCount = cat.getItems() != null ? cat.getItems().size() : 0;
                        System.out.println(
                            (cat.getCategoryName() != null ? cat.getCategoryName() : "Unnamed category")
                            + ": " + itemCount + " items"
                        );
                    }

                    // Sanitize checklist before saving
                    List<ChecklistCategory> cleanedChecklist = checklist.stream()
                            .filter(Objects::nonNull)
                            .toList();
                    plan.setPackingChecklist(cleanedChecklist);
                } else {
                    System.out.println("packingChecklist size: null");
                }
            } else {
                System.out.println("TravelPlan found: false");
            }

            String id = sessionService.saveTravelPlan(plan);
            System.out.println("Generated plan id: " + id);
            return ResponseEntity.ok(Map.of("id", id));
        } catch (Exception e) {
            System.err.println("ERROR in /api/v1/save-plan:");
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Save Failed",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/api/v1/plan/{id}")
    public TravelPlan getPlan(@PathVariable String id) {
        TravelPlan plan = sessionService.getTravelPlan(id);
        if (plan != null) {
            System.out.println("TravelPlan (Shared) found: true");
            TripContext context = plan.getContext();
            if (context != null) {
                System.out.println("tripType: " + context.getTripType());
                System.out.println("children: " + (context.getChildren() != null ? context.getChildren().size() : 0));
                System.out.println("weather: " + context.getWeather());
            }
            List<ChecklistCategory> checklist = plan.getPackingChecklist();
            if (checklist != null) {
                System.out.println("packingChecklist size: " + checklist.size());
                for (ChecklistCategory cat : checklist) {
                    if (cat == null) {
                        System.out.println("Skipping null checklist category");
                        continue;
                    }
                    int itemCount = cat.getItems() != null ? cat.getItems().size() : 0;
                    System.out.println(
                        (cat.getCategoryName() != null ? cat.getCategoryName() : "Unnamed category")
                        + ": " + itemCount + " items"
                    );
                }
            } else {
                System.out.println("Categories: 0");
            }
        }
        return plan;
    }


    @GetMapping("/api/v1/generate-test")
    public ResponseEntity<?> generateTest() {
        TripContext context = new TripContext();
        context.setTripType(TripType.FLIGHT);
        context.setChildren(List.of(new ChildDetail(18))); // 18 months
        context.setWeather(WeatherType.HOT);
        return generateTravelPlan(context);
    }

    @GetMapping("/products-count")
    public Map<String, Integer> getProductsCount() {
        return Map.of("count", productService.getAllProducts().size());
    }
}

