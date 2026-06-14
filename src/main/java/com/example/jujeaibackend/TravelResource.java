package com.example.jujeaibackend;

public class TravelResource {
    private String type;
    private String name;
    private String description;
    private String link;

    public TravelResource() {}

    public TravelResource(String type, String name, String description, String link) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.link = link;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}