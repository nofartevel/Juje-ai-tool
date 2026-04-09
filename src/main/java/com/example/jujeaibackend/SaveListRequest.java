package com.example.jujeaibackend;

import java.util.List;
import java.util.Map;

public class SaveListRequest {
    private String input;
    private List<Product> products;

    public SaveListRequest() {
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}