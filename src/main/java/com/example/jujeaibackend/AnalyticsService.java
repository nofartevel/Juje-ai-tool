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
        session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Session Started", "", LocalDateTime.now()));
        if (context != null) {
            updateSessionContext(session, context);
        }
        sessions.put(sessionId, session);
    }

    private void updateSessionContext(SessionAnalytics session, TripContext context) {
        if (context.getTripType() != null) session.setTripType(context.getTripType());
        if (context.getTransportType() != null) session.setTransportType(context.getTransportType());
        if (context.getDestinationTypes() != null) session.setDestinationTypes(new ArrayList<>(context.getDestinationTypes()));
        if (context.getChildren() != null) session.setChildrenAges(new ArrayList<>(context.getChildren()));
        if (context.getWeather() != null) session.setWeather(context.getWeather());
    }

    public void trackStep(String sessionId, String stepName, TripContext context) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            session.getCompletedSteps().add(new SessionAnalytics.FunnelStep(stepName, LocalDateTime.now()));
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Step Completed", stepName, LocalDateTime.now()));
            if (context != null) {
                updateSessionContext(session, context);
            }
        }
    }

    public void trackImpressions(String sessionId, List<SessionAnalytics.ProductInfo> products) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null && products != null) {
            LocalDateTime now = LocalDateTime.now();
            for (SessionAnalytics.ProductInfo p : products) {
                // Check if already impressed in this session to avoid duplicates
                boolean exists = session.getProductImpressions().stream()
                        .anyMatch(i -> i.getProductId().equals(p.getProductId()));
                if (!exists) {
                    session.getProductImpressions().add(new SessionAnalytics.ProductImpression(p.getProductId(), p.getProductName(), now));
                }
            }
        }
    }

    public void planGenerated(String sessionId, boolean success, String errorMessage, List<SessionAnalytics.ProductInfo> products, List<String> categories) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            session.setGenerationStatus(success ? "SUCCESS" : "FAILED");
            session.setErrorMessage(errorMessage);
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Plan Generated", success ? "SUCCESS" : "FAILED", LocalDateTime.now()));
            if (products != null) {
                session.setGeneratedProducts(products);
                trackImpressions(sessionId, products);
            }
            if (categories != null) {
                session.setChecklistCategories(categories);
            }
        }
    }

    public void productClick(String sessionId, String productId, String productName) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            LocalDateTime now = LocalDateTime.now();
            session.getClickedProducts().add(new SessionAnalytics.ProductClick(productId, productName, now));
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Product Clicked", productName, now));
        }
    }

    public void printClick(String sessionId) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            session.setPrintClicked(true);
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Print Clicked", "", LocalDateTime.now()));
        }
    }

    public void shareClick(String sessionId) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            session.setShareClicked(true);
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Share Clicked", "", LocalDateTime.now()));
        }
    }

    public List<SessionAnalytics> getAllSessions() {
        List<SessionAnalytics> all = new ArrayList<>(sessions.values());
        all.sort(Comparator.comparing(SessionAnalytics::getCreatedAt).reversed());
        return all;
    }
}
