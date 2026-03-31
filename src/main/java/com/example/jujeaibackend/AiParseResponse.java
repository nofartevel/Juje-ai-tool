package com.example.jujeaibackend;

import java.util.List;

public class AiParseResponse {
    private String primary_intent;
    private List<String> secondary_intents;
    private AiKnownInfo known_info;
    private List<String> candidate_tags;
    private String confidence;

    public AiParseResponse() {
    }

    public String getPrimary_intent() {
        return primary_intent;
    }

    public void setPrimary_intent(String primary_intent) {
        this.primary_intent = primary_intent;
    }

    public List<String> getSecondary_intents() {
        return secondary_intents;
    }

    public void setSecondary_intents(List<String> secondary_intents) {
        this.secondary_intents = secondary_intents;
    }

    public AiKnownInfo getKnown_info() {
        return known_info;
    }

    public void setKnown_info(AiKnownInfo known_info) {
        this.known_info = known_info;
    }

    public List<String> getCandidate_tags() {
        return candidate_tags;
    }

    public void setCandidate_tags(List<String> candidate_tags) {
        this.candidate_tags = candidate_tags;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}