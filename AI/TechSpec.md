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
The AI prompt is optimized for performance by reducing input size and delegating static content to the backend.

### 3.1. Performance Optimization Strategy
*   **Prompt Reduction**: Only essential product metadata (ID, name, section, keywords) is sent to OpenAI, reducing input tokens.
*   **Hybrid Content Generation**:
    *   **Static Content**: The backend maintains templates for common items (e.g., passports, diapers) based on `tripType`.
    *   **Dynamic Content**: AI generates only age-specific and destination-specific items, tips, and reminders.
*   **Output Constraint**: AI returns only product IDs; backend enriches with full metadata. AI output is strictly limited to max 5 tips, 5 reminders, and concise checklist items (max 3 categories, max 5 items each).
*   **Model Selection**: Uses `GPT_4O_MINI` (or fastest available model) for minimal latency.
*   **Response Shaping**: AI is instructed to omit common essentials already covered by static templates, reducing output tokens and generation time.

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

### 3.2. Response Shaping
*   **Prompt Instructions**: AI is instructed to omit common essentials already covered by static templates, reducing output tokens and generation time.

### 3.3. System Prompt Strategy
*   **Role**: Expert Family Travel Planner.
*   **Input**: Structured `TripContext` and reduced `Product Bank`.
*   **Output**: Strict JSON format matching `TravelPlan`.
*   **Constraints**: Limit `forgottenItems` and `travelTips` to exactly 5 high-impact items each.
*   **Precision**: Use `ageMonths` to tailor advice.
*   **Destination**: Generate specific preparation tips if `destination` is provided.

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
