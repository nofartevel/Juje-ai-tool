package com.example.jujeaibackend;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import java.util.*;
import org.springframework.http.ResponseEntity;

@RestController
public class HomeController {

    private final ProductService productService;
    private final SessionService sessionService;
    private final OpenAiSelectorService openAiSelectorService;
    private final AnalyticsService analyticsService;

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
            context = new TripContext();
            try {
                if (contextMap.get("tripType") != null) {
                    context.setTripType(TripType.valueOf((String) contextMap.get("tripType")));
                }
                if (contextMap.get("weather") != null) {
                    context.setWeather(WeatherType.valueOf((String) contextMap.get("weather")));
                }
                if (contextMap.get("durationDays") != null) {
                    context.setDurationDays((Integer) contextMap.get("durationDays"));
                }
                
                List<Map<String, Integer>> childrenRaw = (List<Map<String, Integer>>) contextMap.get("children");
                if (childrenRaw != null) {
                    List<ChildDetail> children = new ArrayList<>();
                    for (Map<String, Integer> c : childrenRaw) {
                        children.add(new ChildDetail(c.get("ageInMonths") != null ? c.get("ageInMonths") : c.get("ageMonths")));
                    }
                    context.setChildren(children);
                }
            } catch (Exception e) {
                System.err.println("Error mapping TripContext for analytics: " + e.getMessage());
            }
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
        if (plan != null) {
            System.out.println("TravelPlan found: true");
            TripContext context = plan.getContext();
            if (context != null) {
                System.out.println("tripType: " + context.getTripType());
                System.out.println("children: " + (context.getChildren() != null ? context.getChildren().size() : 0));
                System.out.println("durationDays: " + context.getDurationDays());
                System.out.println("weather: " + context.getWeather());
            }
            List<ChecklistCategory> checklist = plan.getPackingChecklist();
            if (checklist != null) {
                System.out.println("packingChecklist size: " + checklist.size());
                System.out.println("");
                for (ChecklistCategory cat : checklist) {
                    int itemCount = cat.getItems() != null ? cat.getItems().size() : 0;
                    System.out.println(cat.getCategoryName() + ": " + itemCount + " items");
                }
            } else {
                System.out.println("Categories: 0");
            }
        } else {
            System.out.println("TravelPlan found: false");
        }

        String id = sessionService.saveTravelPlan(plan);
        return ResponseEntity.ok(Map.of("id", id));
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
                System.out.println("durationDays: " + context.getDurationDays());
                System.out.println("weather: " + context.getWeather());
            }
            List<ChecklistCategory> checklist = plan.getPackingChecklist();
            if (checklist != null) {
                System.out.println("packingChecklist size: " + checklist.size());
                System.out.println("");
                for (ChecklistCategory cat : checklist) {
                    int itemCount = cat.getItems() != null ? cat.getItems().size() : 0;
                    System.out.println(cat.getCategoryName() + ": " + itemCount + " items");
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
        context.setDurationDays(7);
        context.setWeather(WeatherType.HOT);
        return generateTravelPlan(context);
    }

    @GetMapping("/products-count")
    public Map<String, Integer> getProductsCount() {
        return Map.of("count", productService.getAllProducts().size());
    }
}

