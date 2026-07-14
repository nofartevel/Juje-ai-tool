package com.example.jujeaibackend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionAnalytics {
    private String sessionId;
    private LocalDateTime createdAt;
    private TripType tripType;
    private TransportType transportType;
    private List<DestinationType> destinationTypes;
    private List<ChildDetail> childrenAges;
    private WeatherType weather;
    private List<ProductInfo> generatedProducts = new ArrayList<>();
    private List<ProductClick> clickedProducts = new ArrayList<>();
    private List<String> checklistCategories = new ArrayList<>();
    private Boolean printClicked = false;
    private Boolean shareClicked = false;
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
    public TransportType getTransportType() { return transportType; }
    public void setTransportType(TransportType transportType) { this.transportType = transportType; }
    public List<DestinationType> getDestinationTypes() { return destinationTypes; }
    public void setDestinationTypes(List<DestinationType> destinationTypes) { this.destinationTypes = destinationTypes; }
    public List<ChildDetail> getChildrenAges() { return childrenAges; }
    public void setChildrenAges(List<ChildDetail> childrenAges) { this.childrenAges = childrenAges; }
    public WeatherType getWeather() { return weather; }
    public void setWeather(WeatherType weather) { this.weather = weather; }
    public List<ProductInfo> getGeneratedProducts() { return generatedProducts; }
    public void setGeneratedProducts(List<ProductInfo> generatedProducts) { this.generatedProducts = generatedProducts; }
    public List<ProductClick> getClickedProducts() { return clickedProducts; }
    public void setClickedProducts(List<ProductClick> clickedProducts) { this.clickedProducts = clickedProducts; }
    public List<String> getChecklistCategories() { return checklistCategories; }
    public void setChecklistCategories(List<String> checklistCategories) { this.checklistCategories = checklistCategories; }
    public Boolean getPrintClicked() { return printClicked; }
    public void setPrintClicked(Boolean printClicked) { this.printClicked = printClicked; }
    public boolean isPrintClicked() { return Boolean.TRUE.equals(printClicked); }
    public Boolean getShareClicked() { return shareClicked; }
    public void setShareClicked(Boolean shareClicked) { this.shareClicked = shareClicked; }
    public boolean isShareClicked() { return Boolean.TRUE.equals(shareClicked); }
    public String getGenerationStatus() { return generationStatus; }
    public void setGenerationStatus(String generationStatus) { this.generationStatus = generationStatus; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
