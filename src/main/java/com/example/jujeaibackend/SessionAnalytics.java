package com.example.jujeaibackend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionAnalytics {
    private String visitorId;
    private String sessionId;
    private LocalDateTime createdAt;
    private List<PlanAttempt> planAttempts = new ArrayList<>();
    private List<FunnelStep> completedSteps = new ArrayList<>();
    private List<ProductImpression> productImpressions = new ArrayList<>();
    private List<AnalyticsEvent> timeline = new ArrayList<>();

    public static class PlanAttempt {
        private LocalDateTime timestamp;
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

        public PlanAttempt() {
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters for PlanAttempt
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
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
        public Boolean getShareClicked() { return shareClicked; }
        public void setShareClicked(Boolean shareClicked) { this.shareClicked = shareClicked; }
        public String getGenerationStatus() { return generationStatus; }
        public void setGenerationStatus(String generationStatus) { this.generationStatus = generationStatus; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class FunnelStep {
        private String stepName;
        private LocalDateTime timestamp;

        public FunnelStep() {}
        public FunnelStep(String stepName, LocalDateTime timestamp) {
            this.stepName = stepName;
            this.timestamp = timestamp;
        }

        public String getStepName() { return stepName; }
        public void setStepName(String stepName) { this.stepName = stepName; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class ProductImpression {
        private String productId;
        private String productName;
        private LocalDateTime timestamp;

        public ProductImpression() {}
        public ProductImpression(String productId, String productName, LocalDateTime timestamp) {
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

    public static class AnalyticsEvent {
        private String name;
        private String detail;
        private LocalDateTime timestamp;

        public AnalyticsEvent() {}
        public AnalyticsEvent(String name, String detail, LocalDateTime timestamp) {
            this.name = name;
            this.detail = detail;
            this.timestamp = timestamp;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

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
    public String getVisitorId() { return visitorId; }
    public void setVisitorId(String visitorId) { this.visitorId = visitorId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<PlanAttempt> getPlanAttempts() { return planAttempts; }
    public void setPlanAttempts(List<PlanAttempt> planAttempts) { this.planAttempts = planAttempts; }

    public List<FunnelStep> getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(List<FunnelStep> completedSteps) { this.completedSteps = completedSteps; }

    public List<ProductImpression> getProductImpressions() { return productImpressions; }
    public void setProductImpressions(List<ProductImpression> productImpressions) { this.productImpressions = productImpressions; }

    public List<AnalyticsEvent> getTimeline() { return timeline; }
    public void setTimeline(List<AnalyticsEvent> timeline) { this.timeline = timeline; }
}
