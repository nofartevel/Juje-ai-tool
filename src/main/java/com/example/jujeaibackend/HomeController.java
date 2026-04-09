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

        java.util.List<Product> selectedProducts = productService.getProductsByIds(selectorResult.getSelectedProductIds());

        return new GeneratedListResponse(
                selectorResult.getStatus(),
                selectorResult.getMessage(),
                selectedProducts
        );
    }

    @GetMapping("/ai-generate-test")
    public GeneratedListResponse aiGenerateTest() {
        AiSelectorRequest request = new AiSelectorRequest();
        request.setInput("beach day with toddler");
        return aiGenerateList(request);
    }
}

