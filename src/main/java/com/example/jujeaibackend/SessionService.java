package com.example.jujeaibackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@Service
public class SessionService {

    private final Map<String, SearchSession> savedLists = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
}