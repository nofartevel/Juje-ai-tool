package com.example.jujeaibackend;

import java.util.ArrayList;
import java.util.List;

public class AiSelectorResult {
    private String status; // good / partial / missing
    private String message;
    private List<String> selected_product_ids = new ArrayList<>();

    public AiSelectorResult() {
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getSelected_product_ids() {
        return selected_product_ids;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSelected_product_ids(List<String> selected_product_ids) {
        this.selected_product_ids = selected_product_ids != null ? selected_product_ids : new ArrayList<>();
    }
}