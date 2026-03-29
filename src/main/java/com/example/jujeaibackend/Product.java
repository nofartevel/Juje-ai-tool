package com.example.jujeaibackend;

import java.util.List;

public class Product {

    private String name;
    private String link;
    private String image;
    private List<String> tags;
    private String section;
    private String priority;
    private String why;

    public Product() {
    }

    public Product(String name, String link, String image, List<String> tags, String section, String priority, String why) {
        this.name = name;
        this.link = link;
        this.image = image;
        this.tags = tags;
        this.section = section;
        this.priority = priority;
        this.why = why;
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

    public List<String> getTags() {
        return tags;
    }

    public String getSection() {
        return section;
    }

    public String getPriority() {
        return priority;
    }

    public String getWhy() {
        return why;
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

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setWhy(String why) {
        this.why = why;
    }
}