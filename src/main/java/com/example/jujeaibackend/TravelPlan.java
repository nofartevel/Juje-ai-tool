package com.example.jujeaibackend;

import java.util.List;

public class TravelPlan {
    private String id;
    private List<ChecklistCategory> packingChecklist;
    private List<String> forgottenItems;
    private List<String> travelTips;
    private List<Product> recommendedProducts;
    private List<TravelResource> travelResources;
    private TripContext context;

    public TravelPlan() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<ChecklistCategory> getPackingChecklist() { return packingChecklist; }
    public void setPackingChecklist(List<ChecklistCategory> packingChecklist) { this.packingChecklist = packingChecklist; }
    public List<String> getForgottenItems() { return forgottenItems; }
    public void setForgottenItems(List<String> forgottenItems) { this.forgottenItems = forgottenItems; }
    public List<String> getTravelTips() { return travelTips; }
    public void setTravelTips(List<String> travelTips) { this.travelTips = travelTips; }
    public List<Product> getRecommendedProducts() { return recommendedProducts; }
    public void setRecommendedProducts(List<Product> recommendedProducts) { this.recommendedProducts = recommendedProducts; }
    public List<TravelResource> getTravelResources() { return travelResources; }
    public void setTravelResources(List<TravelResource> travelResources) { this.travelResources = travelResources; }
    public TripContext getContext() { return context; }
    public void setContext(TripContext context) { this.context = context; }
}
