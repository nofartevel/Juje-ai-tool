package com.example.jujeaibackend;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnalyticsService {
    private final Map<String, SessionAnalytics> sessions = new ConcurrentHashMap<>();

    public void startSession(String sessionId, TripContext context) {
        SessionAnalytics session = new SessionAnalytics();
        session.setSessionId(sessionId);
        session.setCreatedAt(LocalDateTime.now());
        if (context != null) {
            session.setTripType(context.getTripType());
            session.setTransportType(context.getTransportType());
            session.setDestinationTypes(context.getDestinationTypes());
            if (context.getChildren() != null) {
                // Defensive copy to ensure we capture current state
                session.setChildrenAges(new ArrayList<>(context.getChildren()));
            }
            session.setWeather(context.getWeather());
        }
        sessions.put(sessionId, session);
    }

    public void planGenerated(String sessionId, boolean success, String errorMessage, List<SessionAnalytics.ProductInfo> products, List<String> categories) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            session.setGenerationStatus(success ? "SUCCESS" : "FAILED");
            session.setErrorMessage(errorMessage);
            if (products != null) {
                session.setGeneratedProducts(products);
            }
            if (categories != null) {
                session.setChecklistCategories(categories);
            }
        }
    }

    public void productClick(String sessionId, String productId, String productName) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            session.getClickedProducts().add(new SessionAnalytics.ProductClick(productId, productName, LocalDateTime.now()));
        }
    }

    public void printClick(String sessionId) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            session.setPrintClicked(true);
        }
    }

    public void shareClick(String sessionId) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            session.setShareClicked(true);
        }
    }

    public List<SessionAnalytics> getAllSessions() {
        List<SessionAnalytics> all = new ArrayList<>(sessions.values());
        all.sort(Comparator.comparing(SessionAnalytics::getCreatedAt).reversed());
        return all;
    }
}
