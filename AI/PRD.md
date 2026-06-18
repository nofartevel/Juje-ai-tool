# PRD: Family Travel Planning Assistant

## 1. Product Vision
Pivot the existing product recommendation tool into a dedicated travel planning assistant for families. The goal is to provide high value to parents by helping them prepare for trips through personalized checklists, tips, and reminders, with product recommendations serving as a supportive secondary feature.

## 2. Target Audience
Parents traveling with children (infants, toddlers, and young kids).

## 3. User Flow (The Wizard)
The app features a high-impact landing page that immediately communicates value, followed by a 3-step guided wizard with persistent navigation and progress indicators:
1.  **High-Impact Landing**: Headline "Traveling with kids? We've got you covered ✈️" and a non-interactive value list (Checklists, Tips, Reminders, Gear).
2.  **Step 1: Trip Type Selection**: Visual cards for Flight, Road Trip, Beach Vacation, Cruise, Camping, General Travel.
3.  **Step 2: Family Details**: User-friendly age entry (Months/Years) optimized for mobile. Includes "Back" button to Step 1.
4.  **Step 3: Trip Details**: Duration (numeric input) and Weather (visual cards). Includes "Back" button to Step 2.
5.  **Plan Generation**: Engaging loading screen with travel-themed animations.
6.  **Results Dashboard**: A premium, context-aware travel plan presented as a modern dashboard with a clear hierarchy:
    *   **Packing Checklist**: Interactive and open by default for immediate action.
    *   **Recommended Gear**: High-visibility product display grouped by categories (e.g., Travel Essentials, Entertainment).
    *   **Travel Tips**: Personalized, actionable advice (collapsible).
    *   **Top Reminders**: Critical "Don't Forget" items (collapsible).
7.  **Export/Print**: User can print the checklist.
8.  **Native Sharing**: User can share the plan using the native mobile share sheet or by copying a link.

## 4. Functional Requirements

### 4.1. Persistent Navigation & Progress
*   **Sticky Header**: Contains JUJE logo and current step label (e.g., "Family • Step 2 of 3").
*   **Progress Indicator**: A visual progress bar showing completion status throughout the wizard.
*   **Navigation**: "Back" buttons on every screen (except the first) to allow users to edit previous selections without loss of state.

### 4.2. Visual Selection Components
*   **Weather Selector**: Visual cards (Hot, Mixed, Cold) with icons instead of dropdowns for better mobile usability.
*   **Duration Input**: Numeric-only input field optimized for mobile keyboards, replacing sliders or generic text fields.

### 4.3. Results Dashboard
*   **Dashboard Layout**: Content is organized into distinct, polished sections in a dashboard hierarchy.
*   **Collapsed Sections**: All primary sections (Checklist, Gear, Tips, Reminders) are collapsed by default to maximize scanability and provide a "dashboard" feel.
*   **Priority Hierarchy**:
    1.  **Full Packing Checklist**: Interactive list with categories (Collapsed by default).
    2.  **Categorized Gear**: Comprehensive product guide grouped by utility (Collapsed by default).
    3.  **Personalized Tips**: Actionable, age-specific advice (Collapsed by default).
    4.  **Things You'll Forget**: High-impact reminders (Collapsed by default).
*   **Modern Gear Section**:
    - Products are grouped into logical categories (e.g., Travel Essentials, Entertainment).
    - **Premium Rows**: Horizontal, fully-clickable list rows with high-quality thumbnails and lightweight metadata tags (e.g., "Best for toddlers", "Flight essential").
    - **Clean UI**: Removed excessive borders and boxes for a lighter, "Airbnb-style" consumer experience.
*   **Visual Hierarchy**: Clear separation between sections with modern spacing, borderless headers, and polished typography.

### 4.4. Personalized Packing Checklist
*   Organized by categories (e.g., Documents, Feeding, Diapering, Sleep, Health & Safety).
*   Interactive checklist items (e.g., "☐ Passports", "☐ Extra outfit for carry-on").
*   **High Personalization**: Checklist items should reference child age and weather (e.g., "Lightweight layer for [Child's Age] for the heat").
*   **Print/Export Support**: Option to print or export the checklist to PDF for offline use.

### 4.2. Top 5 Things Parents Forget
*   Limit to exactly 5 high-value, practical reminders.
*   **Context-Aware**: Reminders must be specific to the trip scenario (e.g., "Download videos before the [Trip Type]").

### 4.3. Personalized Travel Tips
*   Limit to exactly 5 high-impact, actionable pieces of advice.
*   **Specific Advice**: Tips must mention child ages and trip circumstances to provide value beyond a generic search.

### 4.4. Recommended Products
*   Curated selection from the existing product bank.
*   Must be relevant to the specific trip scenario and child age.
*   **Categorized Display**: Products are grouped into logical categories (e.g., Travel Essentials, Hot Weather, Feeding, Entertainment) to improve navigation.
*   **Extended Visibility**: Shows all relevant matching products without artificial limits, encouraging exploration of the catalog.

### 4.6. Native Mobile Sharing
*   **Web Share API**: Integration with the native mobile share sheet for seamless sharing to WhatsApp, Messenger, etc.
*   **Desktop Fallback**: Automated link copying for browsers that do not support native sharing.

## 5. Success Metrics
*   User engagement with the checklist (items checked).
*   Plan saves/shares.
*   CTR (Click-Through Rate) on recommended products.

## 6. Performance Requirements
*   **Quality First**: Personalization and relevance are the primary goals, even if generation takes 10–15 seconds.
*   **Progressive Loading**: The UI uses a travel-themed loading state with rotating messages to manage user expectations during the 10–15s wait.
*   **Reliable Recommendations**: The system always provides at least 4 product recommendations through a hybrid AI + Backend fallback mechanism.
*   **Hybrid Content**: Fast backend templates combined with focused, high-quality AI generation.
