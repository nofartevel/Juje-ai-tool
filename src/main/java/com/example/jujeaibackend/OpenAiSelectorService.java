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

    public AiSelectorResult selectProducts(String userInput) {
        try {
            List<Product> products = productService.getAllProducts();

            String productsJson = objectMapper.writeValueAsString(
                    products.stream().map(p -> Map.of(
                            "id", p.getId(),
                            "name", p.getName(),
                            "section", p.getSection(),
                            "why", p.getWhy(),
                            "keywords", p.getKeywords()
                    )).toList()
            );

            String prompt = """
                You are helping choose products from a curated baby/toddler product bank.
        
                The user wrote a request.
                You must choose only from the provided products.
        
                Be strict.
        
                A product should be selected only if it would realistically belong in a curated list for that exact scenario.
                Do NOT include items that are merely somewhat useful, loosely related, or generally useful for parenting.
                Do NOT include generic food, travel, or diapering items unless they are clearly a strong fit for the exact request.
        
                For specific scenarios, prioritize scenario-specific usefulness over general usefulness.
        
                Status rules:
                - good = the bank clearly covers the request with several strong matches
                - partial = only a few items are genuinely relevant, or coverage is incomplete
                - missing = there is no real coverage for this request
        
                Selection rules:
                - Use ONLY product ids from the product list
                - Return at most 8 ids
                - Prefer returning fewer items over adding weak ones
                - If an item is borderline, leave it out
                - If most candidate products are generic rather than scenario-specific, do NOT return good
        
                Return ONLY valid JSON in this exact shape:
                {
                  "status": "good | partial | missing",
                  "message": "short user-facing message",
                  "selected_product_ids": ["id1", "id2"]
                }
        
                User request:
                "%s"
        
                Product bank:
                %s
                """.formatted(userInput, productsJson);

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