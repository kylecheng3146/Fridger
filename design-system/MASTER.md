# Fridger Design System (Master)

> [!NOTE]
> **Product**: Fridger (Ingredient Manager)
> **Style**: Minimal, Clean, Fresh
> **Tech Stack**: Kotlin Multiplatform (Compose)

## 1. Color Palette

### Primary (Freshness & Nature)

- **Primary**: `#10B981` (Emerald 500) - Main actions, active states
- **On Primary**: `#FFFFFF`
- **Primary Container**: `#D1FAE5` (Emerald 100)
- **On Primary Container**: `#065F46` (Emerald 800)

### Secondary (Food & Warmth)

- **Secondary**: `#F59E0B` (Amber 500) - Accents, warnings (expiry soon), favorites
- **On Secondary**: `#FFFFFF`
- **Secondary Container**: `#FEF3C7` (Amber 100)
- **On Secondary Container**: `#92400E` (Amber 800)

### Surface & Background (Minimalist)

- **Background**:
    - Light: `#FAFAFA` (Zinc 50) - Slightly off-white for softness
    - Dark: `#0F172A` (Slate 900)
- **Surface**:
    - Light: `#FFFFFF`
    - Dark: `#1E293B` (Slate 800)
- **Outline**:
    - Light: `#E4E4E7` (Zinc 200)
    - Dark: `#334155` (Slate 700)

### Functional

- **Error**: `#EF4444` (Red 500) - Expired items
- **Success**: `#10B981` (Emerald 500)
- **Warning**: `#F59E0B` (Amber 500) - Nearing expiry

## 2. Typography

**Font Family**: System Default (San Francisco on iOS, Roboto on Android) for native feel and performance.

| Role            | Size | Weight   | Line Height | Usage                        |
| --------------- | ---- | -------- | ----------- | ---------------------------- |
| Display Large   | 32sp | Bold     | 1.2         | Empty states, Onboarding     |
| Headline Medium | 24sp | SemiBold | 1.3         | Page Titles (e.g., "Fridge") |
| Title Medium    | 18sp | Medium   | 1.4         | Section Headers, Card Titles |
| Body Large      | 16sp | Regular  | 1.5         | Main content, List items     |
| Body Medium     | 14sp | Regular  | 1.5         | Secondary text, descriptions |
| Label Medium    | 12sp | Medium   | 1.2         | Chips, Metadata, Captions    |

## 3. Shape & Spacing

### Shapes (Soft & Organic)

- **Card/Surface**: `RoundedCornerShape(16.dp)`
- **Buttons (Fill)**: `RoundedCornerShape(12.dp)` or `CircleShape` (for icons)
- **Input Fields**: `RoundedCornerShape(12.dp)`
- **Badges/Chips**: `RoundedCornerShape(8.dp)`

### Spacing (8pt Grid)

- **Extra Small**: 4.dp
- **Small**: 8.dp
- **Medium**: 16.dp
- **Large**: 24.dp
- **Extra Large**: 32.dp

## 4. UI Patterns & Best Practices

### Layout

- **Padding**: Standard content padding `16.dp` horizontal.
- **Lists**: Use `LazyColumn` with `contentPadding` to avoid clipping behind navigation bars.
- **Cards**: Use `CardDefaults.elevatedCardColors` with low elevation (1.dp or 2.dp) for clean look. Avoid heavy shadows.
- **Glassmorphism**: Not primary, but can use `Color.White.copy(alpha = 0.9f)` for top bars or floating overlays if requested.

### Interaction

- **Touch Targets**: Minimum `48.dp` for all interactive elements.
- **Feedback**: Use standard ripples.
- **Transitions**: Screens slide in/out horizontally on mobile. Elements fade + scale for visibility changes.

### Components

- **Ingredient Cards**:
    - Minimalist layout: Icon (Left), Name + Qty (Middle), Expiry Badge (Right).
    - **Expiry Badge**: Color-coded pill (Green = OK, Amber = Soon, Red = Expired).
- **Navigation**:
    - Bottom Navigation for mobile (Home, Shopping List, Recipes, Settings).
    - Rail for Tablet/Desktop.

### Accessibility (A11y)

- **Contrast**: Ensure text on colored backgrounds passes 4.5:1.
- **Clickable**: All cards should use `.clip(Shape).clickable { }`.

## 5. Compose Implementation Guide

```kotlin
// Theme Setup
val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

MaterialTheme(
    colorScheme = colorScheme,
    shapes = AppShapes,
    typography = AppTypography
) {
    // Content
}

// Common Modifiers
Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surface)
```

## 6. Iconography

- Use **Filled** icons for active states.
- Use **Outlined** icons for inactive states.
- Consistent 24dp size.
