package com.example.jujeaibackend;

import java.util.List;

public class Question {
    private String id;
    private String text;
    private String type;
    private List<String> options;

    public Question() {
    }

    public Question(String id, String text, String type, List<String> options) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public List<String> getOptions() {
        return options;
    }
}