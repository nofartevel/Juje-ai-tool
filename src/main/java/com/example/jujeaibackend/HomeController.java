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

    public HomeController(ProductService productService, SessionService sessionService) {
        this.productService = productService;
        this.sessionService = sessionService;
    }

    @GetMapping("/recommend")
    public List<Product> recommend(@RequestParam String input) {
        List<String> matchedTags = new ArrayList<>();
        String lowerInput = input.toLowerCase();

        if (lowerInput.contains("flight")) matchedTags.add("flight");
        if (lowerInput.contains("baby")) matchedTags.add("baby");
        if (lowerInput.contains("travel")) matchedTags.add("travel");
        if (lowerInput.contains("feeding")) matchedTags.add("feeding");
        if (lowerInput.contains("park")) matchedTags.add("park");
        if (lowerInput.contains("outdoor")) matchedTags.add("outdoor");

        List<Product> results = new ArrayList<>();

        for (Product product : productService.getAllProducts()) {
            for (String tag : product.getTags()) {
                if (matchedTags.contains(tag)) {
                    results.add(product);
                    break;
                }
            }
        }

        return results;
    }

    @GetMapping("/flow")
    public FlowResponse flow(@RequestParam String input) {
        String lowerInput = input.toLowerCase();

        if (lowerInput.contains("trip") ||
                lowerInput.contains("travel") ||
                lowerInput.contains("flight") ||
                lowerInput.contains("vacation")) {

            return new FlowResponse(
                    "travel",
                    List.of(
                            new Question(
                                    "transport",
                                    "How are you traveling?",
                                    "single-select",
                                    List.of("flight", "car", "cruise", "mixed")
                            ),
                            new Question(
                                    "age_groups",
                                    "What age groups are you shopping for?",
                                    "multi-select",
                                    List.of("0-6 months", "6-12 months", "12-24 months", "2+ years")
                            ),
                            new Question(
                                    "feeding",
                                    "How do you feed your baby?",
                                    "single-select",
                                    List.of("breastfeeding", "formula", "solids", "combination")
                            ),
                            new Question(
                                    "trip_length",
                                    "How long is the trip?",
                                    "single-select",
                                    List.of("1 day", "weekend", "1 week+")
                            )
                    )
            );
        }

        if (lowerInput.contains("park") || lowerInput.contains("outdoor") || lowerInput.contains("playground")) {
            return new FlowResponse(
                    "outdoor",
                    List.of(
                            new Question(
                                    "place_type",
                                    "Where are you going?",
                                    "single-select",
                                    List.of("playground", "park", "beach", "mixed")
                            ),
                            new Question(
                                    "age_groups",
                                    "What age groups are you shopping for?",
                                    "multi-select",
                                    List.of("0-6 months", "6-12 months", "12-24 months", "2+ years")
                            ),
                            new Question(
                                    "need_type",
                                    "What do you need most?",
                                    "single-select",
                                    List.of("toys", "sitting area", "snacks", "sun protection")
                            )
                    )
            );
        }

        if (lowerInput.contains("play") || lowerInput.contains("activity") || lowerInput.contains("game")) {
            return new FlowResponse(
                    "activities",
                    List.of(
                            new Question(
                                    "age_groups",
                                    "What age groups are you shopping for?",
                                    "multi-select",
                                    List.of("0-6 months", "6-12 months", "12-24 months", "2+ years")
                            ),
                            new Question(
                                    "play_type",
                                    "What kind of play do you want?",
                                    "single-select",
                                    List.of("quiet", "creative", "sensory", "active")
                            ),
                            new Question(
                                    "location",
                                    "Where will they play?",
                                    "single-select",
                                    List.of("indoors", "outdoors", "both")
                            )
                    )
            );
        }

        return new FlowResponse(
                "general",
                List.of(
                        new Question(
                                "clarify",
                                "Tell me a bit more about what you need",
                                "single-select",
                                List.of("travel", "outdoor", "activities")
                        )
                )
        );
    }

    @PostMapping("/recommend-from-answers")
    public List<Product> recommendFromAnswers(@RequestBody Map<String, Object> answers) {

        List<String> tags = new ArrayList<>();

        String transport = (String) answers.get("transport");
        if ("flight".equals(transport)) {
            tags.add("flight");
            tags.add("travel");
        }
        if ("car".equals(transport)) {
            tags.add("car");
            tags.add("travel");
        }
        if ("cruise".equals(transport)) {
            tags.add("cruise");
            tags.add("travel");
        }
        if ("mixed".equals(transport)) {
            tags.add("travel");
        }

        Object ageGroupsObject = answers.get("age_groups");
        if (ageGroupsObject instanceof List<?>) {
            List<?> ageGroups = (List<?>) ageGroupsObject;

            if (ageGroups.contains("0-6 months")) {
                tags.add("baby");
                tags.add("infant");
            }
            if (ageGroups.contains("6-12 months")) {
                tags.add("baby");
            }
            if (ageGroups.contains("12-24 months")) {
                tags.add("toddler");
            }
            if (ageGroups.contains("2+ years")) {
                tags.add("toddler");
                tags.add("kid");
            }
        }

        String feeding = (String) answers.get("feeding");
        if ("breastfeeding".equals(feeding)) {
            tags.add("feeding");
            tags.add("breastfeeding");
        }
        if ("formula".equals(feeding)) {
            tags.add("feeding");
            tags.add("formula");
        }
        if ("solids".equals(feeding)) {
            tags.add("feeding");
            tags.add("solids");
        }
        if ("combination".equals(feeding)) {
            tags.add("feeding");
        }

        String tripLength = (String) answers.get("trip_length");
        if ("weekend".equals(tripLength)) {
            tags.add("travel_short");
        }
        if ("1 week+".equals(tripLength)) {
            tags.add("travel_extended");
        }

        String placeType = (String) answers.get("place_type");
        if ("playground".equals(placeType) || "park".equals(placeType) || "beach".equals(placeType)) {
            tags.add("outdoor");
        }

        String needType = (String) answers.get("need_type");
        if (needType != null) {
            tags.add(needType);
        }

        String playType = (String) answers.get("play_type");
        if (playType != null) {
            tags.add(playType);
        }

        String location = (String) answers.get("location");
        if (location != null) {
            tags.add(location);
        }

        List<Product> results = new ArrayList<>();

        for (Product product : productService.getAllProducts()) {
            for (String tag : product.getTags()) {
                if (tags.contains(tag)) {
                    results.add(product);
                    break;
                }
            }
        }

        return results;
    }

    @PostMapping("/save-list")
    public ResponseEntity<?> saveList(@RequestBody SaveListRequest request) {
        try {
            SearchSession session = sessionService.createSession(
                    request.getInput(),
                    request.getIntent(),
                    request.getAnswers(),
                    request.getProducts()
            );

            try {
                sessionService.logSession(session);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String id = sessionService.saveList(session);
            return ResponseEntity.ok(Map.of("id", id));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "save_failed",
                    "message", e.getMessage() == null ? "Unknown server error" : e.getMessage()
            ));
        }
    }

    @GetMapping("/list/{id}")
    public SearchSession getSavedList(@PathVariable String id) {
        return sessionService.getSavedList(id);
    }
}

