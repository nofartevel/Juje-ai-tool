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

    public HomeController(ProductService productService,
                          SessionService sessionService,
                          OpenAiSelectorService openAiSelectorService) {
        this.sessionService = sessionService;
        this.productService = productService;
        this.openAiSelectorService = openAiSelectorService;
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
        String id = sessionService.saveTravelPlan(plan);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/api/v1/plan/{id}")
    public TravelPlan getPlan(@PathVariable String id) {
        return sessionService.getTravelPlan(id);
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

