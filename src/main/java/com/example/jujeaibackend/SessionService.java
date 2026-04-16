package com.example.jujeaibackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SessionService {

    private final Map<String, SearchSession> savedLists = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Map<String, String>> learningCases = new ArrayList<>();

    public SearchSession createSession(String input, List<Product> products) {

        String id = UUID.randomUUID().toString().substring(0, 8);

        SearchSession session = new SearchSession();
        session.setId(id);
        session.setInput(input);
        session.setProducts(products);
        session.setCreatedAt(LocalDateTime.now().toString());

        return session;
    }

    public void logSession(SearchSession session) {
        try {
            File file = new File("search-log.jsonl");
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.write(objectMapper.writeValueAsString(session));
                writer.write(System.lineSeparator());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveList(SearchSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }

        savedLists.put(session.getId(), session);
        return session.getId();
    }

    public SearchSession getSavedList(String id) {
        return savedLists.get(id);
    }

    public void logLearningCase(String input, String status) {
        if (input == null) return;

        Map<String, String> entry = new HashMap<>();
        entry.put("input", input);
        entry.put("status", status == null ? "unknown" : status);
        entry.put("time", LocalDateTime.now().toString());

        learningCases.add(entry);
    }

    public List<Map<String, String>> getLearningCases() {
        return learningCases;
    }

    public List<Map<String, String>> getLearningCasesByStatus(String status) {
        if (status == null || status.isBlank()) {
            return learningCases;
        }

        return learningCases.stream()
                .filter(entry -> status.equalsIgnoreCase(entry.get("status")))
                .toList();
    }
}