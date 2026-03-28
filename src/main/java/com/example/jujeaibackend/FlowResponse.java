package com.example.jujeaibackend;

import java.util.List;

public class FlowResponse {
    private String intent;
    private List<Question> questions;

    public FlowResponse() {
    }

    public FlowResponse(String intent, List<Question> questions) {
        this.intent = intent;
        this.questions = questions;
    }

    public String getIntent() {
        return intent;
    }

    public List<Question> getQuestions() {
        return questions;
    }
}