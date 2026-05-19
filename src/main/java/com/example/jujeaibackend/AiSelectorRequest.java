package com.example.jujeaibackend;

public class AiSelectorRequest {
    private String input;
    private String category;

    public AiSelectorRequest() {
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}