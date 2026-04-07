package com.example.jujeaibackend;

import java.util.List;

public class GeneratedListResponse {
    private String status;
    private String message;
    private List<Product> products;
    private AiParseResult aiResult;

    public GeneratedListResponse() {
    }

    public GeneratedListResponse(String status, String message, List<Product> products, AiParseResult aiResult) {
        this.status = status;
        this.message = message;
        this.products = products;
        this.aiResult = aiResult;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Product> getProducts() {
        return products;
    }

    public AiParseResult getAiResult() {
        return aiResult;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void setAiResult(AiParseResult aiResult) {
        this.aiResult = aiResult;
    }
}