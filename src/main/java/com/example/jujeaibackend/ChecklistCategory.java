package com.example.jujeaibackend;

import java.util.List;

public class ChecklistCategory {
    private String categoryName;
    private List<String> items;

    public ChecklistCategory() {}

    public ChecklistCategory(String categoryName, List<String> items) {
        this.categoryName = categoryName;
        this.items = items;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
}
