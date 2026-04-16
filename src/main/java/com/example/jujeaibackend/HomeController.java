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

    @PostMapping("/save-list")
    public ResponseEntity<?> saveList(@RequestBody SaveListRequest request) {
        try {
            SearchSession session = sessionService.createSession(
                    request.getInput(),
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

    @PostMapping("/ai-generate-list")
    public GeneratedListResponse aiGenerateList(@RequestBody AiSelectorRequest request) {

        AiSelectorResult selectorResult = openAiSelectorService.selectProducts(request.getInput());

        java.util.List<Product> selectedProducts =
                productService.getProductsByIds(selectorResult.getSelectedProductIds());

        GeneratedListResponse response = new GeneratedListResponse(
                selectorResult.getStatus(),
                selectorResult.getMessage(),
                selectedProducts
        );

        sessionService.logLearningCase(request.getInput(), selectorResult.getStatus());

        return response;
    }

    @GetMapping("/ai-generate-test")
    public GeneratedListResponse aiGenerateTest() {
        AiSelectorRequest request = new AiSelectorRequest();
        request.setInput("beach day with toddler");
        return aiGenerateList(request);
    }

    @GetMapping("/learning")
    public ResponseEntity<?> getLearningCases(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(sessionService.getLearningCasesByStatus(status));
    }

    /*
        ========================================
        LEARNING ENDPOINT USAGE
        ========================================

        This endpoint returns user searches ("learning data")
        Optionally filtered by status.

        Available statuses:
        - good     → strong matches found
        - partial  → some matches but not perfect
        - missing  → no good matches

        ----------------------------------------
        How to use:

        1. Get ALL searches:
           /learning

        2. Get only missing (most important for improving products):
           /learning?status=missing

        3. Get partial (almost good → refine products):
           /learning?status=partial

        4. Get good (successful searches):
           /learning?status=good

        ----------------------------------------
        Notes:

        - If no status is provided → returns ALL data
        - Newest searches appear FIRST in the list
        - Data is stored in memory (temporary):
          → Will RESET on:
             - server restart
             - new deploy
             - crash

        ----------------------------------------
        Goal:

        Use this data to:
        - Identify missing product scenarios
        - Improve product matching
        - Add new relevant products

        ========================================
*/
}

