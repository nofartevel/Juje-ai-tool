package com.example.jujeaibackend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionAnalytics {
    private String sessionId;
    private LocalDateTime createdAt;
    private TripType tripType;
    private List<ChildDetail> childrenAges;
    private WeatherType weather;
    private int durationDays;
    private List<ProductInfo> generatedProducts = new ArrayList<>();
    private List<ProductClick> clickedProducts = new ArrayList<>();
    private List<String> checklistCategories = new ArrayList<>();
    private boolean printClicked;
    private boolean shareClicked;
    private String generationStatus; // "SUCCESS" or "FAILED"
    private String errorMessage;

    public static class ProductInfo {
        private String productId;
        private String productName;

        public ProductInfo() {}
        public ProductInfo(String productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
    }

    public static class ProductClick {
        private String productId;
        private String productName;
        private LocalDateTime timestamp;

        public ProductClick() {}
        public ProductClick(String productId, String productName, LocalDateTime timestamp) {
            this.productId = productId;
            this.productName = productName;
            this.timestamp = timestamp;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public TripType getTripType() { return tripType; }
    public void setTripType(TripType tripType) { this.tripType = tripType; }
    public List<ChildDetail> getChildrenAges() { return childrenAges; }
    public void setChildrenAges(List<ChildDetail> childrenAges) { this.childrenAges = childrenAges; }
    public WeatherType getWeather() { return weather; }
    public void setWeather(WeatherType weather) { this.weather = weather; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public List<ProductInfo> getGeneratedProducts() { return generatedProducts; }
    public void setGeneratedProducts(List<ProductInfo> generatedProducts) { this.generatedProducts = generatedProducts; }
    public List<ProductClick> getClickedProducts() { return clickedProducts; }
    public void setClickedProducts(List<ProductClick> clickedProducts) { this.clickedProducts = clickedProducts; }
    public List<String> getChecklistCategories() { return checklistCategories; }
    public void setChecklistCategories(List<String> checklistCategories) { this.checklistCategories = checklistCategories; }
    public boolean isPrintClicked() { return printClicked; }
    public void setPrintClicked(boolean printClicked) { this.printClicked = printClicked; }
    public boolean isShareClicked() { return shareClicked; }
    public void setShareClicked(boolean shareClicked) { this.shareClicked = shareClicked; }
    public String getGenerationStatus() { return generationStatus; }
    public void setGenerationStatus(String generationStatus) { this.generationStatus = generationStatus; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
