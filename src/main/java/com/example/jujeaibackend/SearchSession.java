package com.example.jujeaibackend;

import java.util.List;

public class SearchSession {
    private String id;
    private String input;
    private List<Product> products;
    private String createdAt;

    public SearchSession() {
    }

    public SearchSession(String id, String input, List<Product> products, String createdAt) {
        this.id = id;
        this.input = input;
        this.products = products;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getInput() {
        return input;
    }

    public List<Product> getProducts() {
        return products;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}