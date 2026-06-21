package com.example.jujeaibackend;

import java.util.List;

public class Product {

    private String id;
    private String name;
    private String link;
    private String image;
    private String section;
    private String why;
    private List<String> keywords;
    private List<String> use_cases;
    private List<String> age_groups;
    private List<String> needs;
    private String activity_type;
    private List<String> trip_types;
    private List<String> weather;
    private boolean is_essential;

    public Product() {
    }

    public Product(String id, String name, String link, String image, String section, String why,
                   List<String> keywords, List<String> use_cases, List<String> age_groups, List<String> needs,
                   List<String> trip_types, List<String> weather, boolean is_essential) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.image = image;
        this.section = section;
        this.why = why;
        this.keywords = keywords;
        this.use_cases = use_cases;
        this.age_groups = age_groups;
        this.needs = needs;
        this.trip_types = trip_types;
        this.weather = weather;
        this.is_essential = is_essential;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String getImage() {
        return image;
    }

    public String getSection() {
        return section;
    }

    public String getWhy() {
        return why;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public List<String> getUse_cases() {
        return use_cases;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setUse_cases(List<String> use_cases) {
        this.use_cases = use_cases;
    }

    public List<String> getAge_groups() {
        return age_groups;
    }

    public void setAge_groups(List<String> age_groups) {
        this.age_groups = age_groups;
    }

    public List<String> getNeeds() {
        return needs;
    }

    public void setNeeds(List<String> needs) {
        this.needs = needs;
    }

    public String getActivity_type() {
        return activity_type;
    }

    public void setActivity_type(String activity_type) {
        this.activity_type = activity_type;
    }

    public List<String> getTrip_types() {
        return trip_types;
    }

    public void setTrip_types(List<String> trip_types) {
        this.trip_types = trip_types;
    }

    public List<String> getWeather() {
        return weather;
    }

    public void setWeather(List<String> weather) {
        this.weather = weather;
    }

    public boolean is_essential() {
        return is_essential;
    }

    public void setIs_essential(boolean is_essential) {
        this.is_essential = is_essential;
    }
}