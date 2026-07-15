package com.example.jujeaibackend;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AnalyticsService {
    private final Map<String, SessionAnalytics> sessions = new ConcurrentHashMap<>();

    public void startSession(String visitorId, String sessionId, TripContext context) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session == null) {
            session = new SessionAnalytics();
            session.setVisitorId(visitorId);
            session.setSessionId(sessionId);
            session.setCreatedAt(LocalDateTime.now());
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Session Started", "", LocalDateTime.now()));
            sessions.put(sessionId, session);
        }
    }

    private void updateAttemptContext(SessionAnalytics.PlanAttempt attempt, TripContext context) {
        if (context.getTripType() != null) attempt.setTripType(context.getTripType());
        if (context.getTransportType() != null) attempt.setTransportType(context.getTransportType());
        if (context.getDestinationTypes() != null) attempt.setDestinationTypes(new ArrayList<>(context.getDestinationTypes()));
        if (context.getChildren() != null) attempt.setChildrenAges(new ArrayList<>(context.getChildren()));
        if (context.getWeather() != null) attempt.setWeather(context.getWeather());
    }

    public void trackStep(String visitorId, String sessionId, String stepName, TripContext context) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session == null) {
            startSession(visitorId, sessionId, context);
            session = sessions.get(sessionId);
        }
        
        if (session != null) {
            session.getCompletedSteps().add(new SessionAnalytics.FunnelStep(stepName, LocalDateTime.now()));
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Step Completed", stepName, LocalDateTime.now()));
            
            if ("Transport Selected".equals(stepName)) {
                // Potential start of a new plan attempt? 
                // Actually, let's create a new plan attempt only when the first step of the flow starts OR when planGenerated is called.
                // The issue says: "Every Generate Plan should create a new PlanAttempt".
            }
        }
    }

    public void trackImpressions(String sessionId, List<SessionAnalytics.ProductInfo> products) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null && products != null) {
            LocalDateTime now = LocalDateTime.now();
            for (SessionAnalytics.ProductInfo p : products) {
                // We track global impressions for the session
                boolean exists = session.getProductImpressions().stream()
                        .anyMatch(i -> i.getProductId().equals(p.getProductId()));
                if (!exists) {
                    session.getProductImpressions().add(new SessionAnalytics.ProductImpression(p.getProductId(), p.getProductName(), now));
                }
            }
        }
    }

    public void planGenerated(String visitorId, String sessionId, boolean success, String errorMessage, List<SessionAnalytics.ProductInfo> products, List<String> categories, TripContext context) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session == null) {
            startSession(visitorId, sessionId, context);
            session = sessions.get(sessionId);
        }
        if (session != null) {
            SessionAnalytics.PlanAttempt attempt = new SessionAnalytics.PlanAttempt();
            attempt.setGenerationStatus(success ? "SUCCESS" : "FAILED");
            attempt.setErrorMessage(errorMessage);
            if (context != null) {
                updateAttemptContext(attempt, context);
            }
            if (products != null) {
                attempt.setGeneratedProducts(products);
                trackImpressions(sessionId, products);
            }
            if (categories != null) {
                attempt.setChecklistCategories(categories);
            }
            session.getPlanAttempts().add(attempt);
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Plan Generated", success ? "SUCCESS" : "FAILED", LocalDateTime.now()));
        }
    }

    public void productClick(String sessionId, String productId, String productName) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            LocalDateTime now = LocalDateTime.now();
            // Add to the latest plan attempt if available
            if (!session.getPlanAttempts().isEmpty()) {
                SessionAnalytics.PlanAttempt lastAttempt = session.getPlanAttempts().get(session.getPlanAttempts().size() - 1);
                lastAttempt.getClickedProducts().add(new SessionAnalytics.ProductClick(productId, productName, now));
            }
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Product Clicked", productName, now));
        }
    }

    public void printClick(String sessionId) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            if (!session.getPlanAttempts().isEmpty()) {
                session.getPlanAttempts().get(session.getPlanAttempts().size() - 1).setPrintClicked(true);
            }
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Print Clicked", "", LocalDateTime.now()));
        }
    }

    public void shareClick(String sessionId) {
        SessionAnalytics session = sessions.get(sessionId);
        if (session != null) {
            if (!session.getPlanAttempts().isEmpty()) {
                session.getPlanAttempts().get(session.getPlanAttempts().size() - 1).setShareClicked(true);
            }
            session.getTimeline().add(new SessionAnalytics.AnalyticsEvent("Share Clicked", "", LocalDateTime.now()));
        }
    }

    public List<SessionAnalytics> getAllSessions() {
        List<SessionAnalytics> all = new ArrayList<>(sessions.values());
        all.sort(Comparator.comparing(SessionAnalytics::getCreatedAt).reversed());
        return all;
    }
}
