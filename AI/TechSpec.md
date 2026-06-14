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
The AI prompt will be redesigned to return a structured JSON containing all four sections. 

### 3.1. System Prompt Strategy
*   **Role**: Expert Family Travel Planner.
*   **Input**: Structured `TripContext` (emphasizing `tripType` and `destination`).
*   **Output**: Strict JSON format matching `TravelPlan`.
*   **Precision**: Use `ageMonths` to tailor advice (infant vs toddler vs child).
*   **Destination**: Generate specific preparation tips if `destination` is provided.
*   **Context**: Include the `products.json` bank for the "Recommended Products" section.

## 4. API Endpoints
*   `POST /api/v1/generate-travel-plan`: Main generation logic.
*   `POST /api/v1/save-plan`: Persist `TravelPlan` to storage (currently in-memory `SessionService`).
*   `GET /api/v1/plan/{id}`: Retrieve saved plans for sharing.

## 5. Frontend Requirements
*   **State Management**: Use a "Wizard" pattern to collect `TripContext`.
*   **Age Input**: Allow parents to enter age in months.
*   **Persistence**: Store the current checklist state (checked items) in `localStorage` to survive page refreshes.
*   **UI Components**: Checklist with accordion categories, Tip cards, Product grid, and Print/PDF export buttons.

## 6. Implementation Phases
1.  **Phase 1**: Backend Refactoring (DTOs, Controller, Service).
2.  **Phase 2**: Prompt Engineering & AI Tuning.
3.  **Phase 3**: Frontend Wizard Implementation.
4.  **Phase 4**: Results View & Sharing Logic.
