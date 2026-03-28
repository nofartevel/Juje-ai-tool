package com.example.jujeaibackend;

import java.util.Map;

public class SaveListRequest {
    private String input;
    private String intent;
    private Map<String, Object> answers;

    public SaveListRequest() {
    }

    public String getInput() {
        return input;
    }

    public String getIntent() {
        return intent;
    }

    public Map<String, Object> getAnswers() {
        return answers;
    }
}