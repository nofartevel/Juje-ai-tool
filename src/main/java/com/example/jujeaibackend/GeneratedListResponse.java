package com.example.jujeaibackend;

import java.util.List;

public class GeneratedListResponse {
    private String status;
    private String message;
    private List<Product> products;

    public GeneratedListResponse() {
    }

    public GeneratedListResponse(String status, String message, List<Product> products) {
        this.status = status;
        this.message = message;
        this.products = products;
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }


}