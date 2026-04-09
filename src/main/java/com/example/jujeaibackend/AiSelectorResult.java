package com.example.jujeaibackend;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AiSelectorResult {
    private String status;
    private String message;

    @JsonProperty("selected_product_ids")
    private List<String> selectedProductIds = new ArrayList<>();

    public AiSelectorResult() {
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getSelectedProductIds() {
        return selectedProductIds;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSelectedProductIds(List<String> selectedProductIds) {
        this.selectedProductIds = selectedProductIds != null ? selectedProductIds : new ArrayList<>();
    }
}