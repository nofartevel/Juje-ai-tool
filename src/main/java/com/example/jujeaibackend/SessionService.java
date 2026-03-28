package com.example.jujeaibackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SessionService {

    private final Map<String, SearchSession> savedLists = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void logSearch(String input, String intent, Map<String, Object> answers, List<Product> products) {
        try {
            SearchSession session = new SearchSession(
                    UUID.randomUUID().toString(),
                    input,
                    intent,
                    answers,
                    products,
                    LocalDateTime.now().toString()
            );

            String jsonLine = objectMapper.writeValueAsString(session);

            try (PrintWriter out = new PrintWriter(new FileWriter("search-log.jsonl", true))) {
                out.println(jsonLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveList(String input, String intent, Map<String, Object> answers, List<Product> products) {
        String id = UUID.randomUUID().toString().substring(0, 8);

        SearchSession session = new SearchSession(
                id,
                input,
                intent,
                answers,
                products,
                LocalDateTime.now().toString()
        );

        savedLists.put(id, session);
        return id;
    }

    public SearchSession getSavedList(String id) {
        return savedLists.get(id);
    }

    public Collection<SearchSession> getAllSavedLists() {
        return savedLists.values();
    }
}