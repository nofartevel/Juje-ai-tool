package com.example.jujeaibackend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SearchSession {
    private String id;
    private String input;
    private String intent;
    private Map<String, Object> answers;
    private List<Product> products;
    private String createdAt;

    public SearchSession() {
    }

    public SearchSession(String id, String input, String intent, Map<String, Object> answers, List<Product> products, String createdAt) {
        this.id = id;
        this.input = input;
        this.intent = intent;
        this.answers = answers;
        this.products = products;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getInput() {
        return input;
    }

    public String getIntent() {
        return intent;
    }

    public Map<String, Object> getAnswers() {
        return answers;
    }

    public List<Product> getProducts() {
        return products;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}