package com.example.jujeaibackend;

import java.util.List;

public class Product {
    private String name;
    private String link;
    private List<String> tags;
    private String why;

    public Product() {
    }

    public Product(String name, String link, List<String> tags, String why) {
        this.name = name;
        this.link = link;
        this.tags = tags;
        this.why = why;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getWhy() {
        return why;
    }
}