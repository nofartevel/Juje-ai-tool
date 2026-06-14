package com.example.jujeaibackend;

import java.util.List;

public class TripContext {
    private TripType tripType;
    private List<ChildDetail> children;
    private int durationDays;
    private String destination;
    private WeatherType weather;

    public TripContext() {}

    public TripType getTripType() { return tripType; }
    public void setTripType(TripType tripType) { this.tripType = tripType; }
    public List<ChildDetail> getChildren() { return children; }
    public void setChildren(List<ChildDetail> children) { this.children = children; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public WeatherType getWeather() { return weather; }
    public void setWeather(WeatherType weather) { this.weather = weather; }
}
