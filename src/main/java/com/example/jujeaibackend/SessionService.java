package com.example.jujeaibackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SessionService {

    private final Map<String, TravelPlan> savedPlans = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String saveTravelPlan(TravelPlan plan) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        plan.setId(id);
        savedPlans.put(id, plan);
        return id;
    }

    public TravelPlan getTravelPlan(String id) {
        return savedPlans.get(id);
    }

}