# Technical Specification: Family Travel Planning Assistant

## 1. System Architecture
The system will leverage the existing Spring Boot backend and OpenAI integration, moving from a single-prompt product selector to a multi-faceted content generator.

## 2. Data Models (DTOs)

### 2.1. Request Context (`TripContext`)
```java
public class TripContext {
    private TripType tripType;
    private List<ChildDetail> children;
    private int durationDays;
    private String destination;
    private WeatherType weather;
}

public class ChildDetail {
    private int ageMonths;
}
```

### 2.2. Response Structure (`TravelPlan`)
```java
public class TravelPlan {
    private String id;
    private List<ChecklistCategory> packingChecklist;
    private List<String> forgottenItems;
    private List<String> travelTips;
    private List<Product> recommendedProducts;
    private List<TravelResource> travelResources; // Future monetization
}

public class TravelResource {
    private String type; // e.g., "eSIM", "Insurance"
    private String name;
    private String description;
    private String link;
}
```

## 3. AI Implementation (`OpenAiSelectorService` Update)
The AI strategy prioritizes **high personalization** over raw generation speed. The target generation time is 10–15 seconds to ensure quality and relevance.

### 3.1. Personalization Strategy
*   **Detailed Context**: The AI receives full `TripContext` including `ageMonths`, `destination`, `weather`, and `tripType`.
*   **Model Selection**: Uses `GPT_4O` for higher quality reasoning and better personalization compared to mini models.
*   **Prompting**: Instructed to explicitly reference age-specific needs and destination-specific preparation advice. The AI MUST weave the destination name into the content and account for its unique characteristics (rain, humidity, terrain, etc.).
*   **Content Focus**: AI generates "Value-Add" items, while common essentials (passports, diapers) are managed by backend templates.

### 3.2. Product Recommendation Strategy
*   **AI-First**: AI selects relevant products from a pre-filtered list based on `tripType`.
*   **Backend Fallback**: If AI returns fewer than 4 products, the backend supplements the list with relevant items from the catalog (matching `tripType` or `GENERAL`) to ensure at least 4 recommendations.
*   **Data Enrichment**: AI returns only IDs; backend enriches with full metadata (images, links, etc.).

### 3.3. Performance & Latency
*   **Optimization**: Products are pre-filtered before being sent to the AI to reduce token usage.
*   **Hybrid Generation**: Combines static templates with dynamic AI content.
*   **Target Latency**: 10–15 seconds.

## 7. Troubleshooting & Resilience
### 7.1. OpenAI Rate Limits & Quota (Error 429)
*   **Cause**: OpenAI API quota exceeded or billing issues.
*   **Backend Strategy**: 
    *   Fail immediately on `RateLimitException` (no retries for quota/billing errors).
    *   Return a user-friendly 429 response.
*   **Frontend UX**: 
    *   Informative alerts: "The travel planner is temporarily unavailable. Please try again later."
    *   Graceful fallback to the intro screen.
*   **Mitigation**: 
    *   Monitor and fix OpenAI billing and usage limits in the OpenAI dashboard.


## 4. API Endpoints
*   `POST /api/v1/generate-travel-plan`: Main generation logic (now enriches product data).
*   `POST /api/v1/save-plan`: Persist `TravelPlan` to storage (currently in-memory `SessionService`).
*   `GET /api/v1/plan/{id}`: Retrieve saved plans for sharing.

## 5. Frontend Requirements
*   **State Management**: Use a "Wizard" pattern to collect `TripContext`.
*   **Age Input**: Allow parents to enter age in months/years; optimized for mobile with numeric keyboards and placeholder-based inputs.
*   **Loading Experience**: Travel-themed animations and progress reassurances based on selected `tripType`.
*   **Result Presentation**:
    *   **Priority Order**: 1. Reminders, 2. Recommended Gear, 3. Quick Tips, 4. Full Packing Checklist.
    *   **Accordion UI**: Packing checklist categories are collapsed by default.
    *   **Mobile-First**: Reduced product card heights and grid layouts.
*   **Export/Share**: 
    *   Print support for physical checklists.
    *   Native mobile sharing via Web Share API with a desktop fallback (prompt).

## 6. Implementation Phases
1.  **Phase 1**: Backend Refactoring (DTOs, Controller, Service).
2.  **Phase 2**: Prompt Engineering & AI Tuning.
3.  **Phase 3**: Frontend Wizard Implementation.
4.  **Phase 4**: Results View & Sharing Logic.
