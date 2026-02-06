---
name: kmp-reviewer
description: Expert Code Reviewer for Kotlin Multiplatform (KMP) projects using Modern Android/KMP best practices. Use this when the user asks to "review code", "check architecture", or "audit KMP" module. It focuses on Clean Architecture, Platform Isolation, Purity, and Concurrency safety.
---

# KMP Code Review Skill

This skill helps you audit Kotlin Multiplatform projects against strict "Pro" standards, checking for architecture violations, platform leakage, and concurrency issues.

## Workflow

### Step 1: Automated "Red Flag" Scan

Run the included script to detect obvious violations in `commonMain`:

```bash
python3 .agent/skills/kmp-reviewer/scripts/scan.py
```

**Analyze the output**:

- **Imports**: Any `android.*`, `java.*`, or `UIKit` in `commonMain` is a critical fail.
- **Dispatchers**: Hardcoded `Dispatchers.Main/IO` suggests missing abstraction (DispatcherProvider).
- **Types**: `Context` or `Activity` in common code implies coupling.

### Step 2: Architecture & Layering Check

Reading the file structure and key files using `view_file`.

- **Domain Layer**: Must be pure Kotlin. Verification: Check `composeApp/src/commonMain/kotlin/.../domain`.
- **Data Layer**: Mappers must convert DTOs (Data) to Domain Models.
- **Dependencies**: Ensure `commonMain` dependency block in `build.gradle.kts` does not include android-specific libraries.

### Step 3: Concurrency & Lifecycle

- Check `ViewModel` or `Presenter` classes in `commonMain`:
    - Are strict scopes used? (e.g., `viewModelScope`)
    - Are `DispatcherProvider`s injected?
    - Are Flows exposed as `StateFlow` or `SharedFlow` (hot flows) for UI?

### Step 4: Comprehensive Checklist Verification

Refer to the detailed rules in `references/kmp_checklist.md` to ensure no subtle issues are missed.

- Check `expect/actual` usage (should be thin wrappers).
- Check Error Handling (usage of `Result` or `sealed class` instead of Exceptions).

## Final Report

Generate a review report for the User grouping findings by:

1.  **Critical Issues** (Architecture violations, Platform leaks)
2.  **Warnings** (Hardcoded dispatchers, lack of tests)
3.  **Suggestions** (Refactoring opportunities)

Use the checklist in `references/kmp_checklist.md` as the standard.
