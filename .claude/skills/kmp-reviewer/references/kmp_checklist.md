# KMP Code Review Checklist

Usage: Use this checklist to verify the architectural integrity and code quality of the KMP project.

## 1. Architecture & Modules (架構與模組切分)

### Shared vs Platform 邊界

- [ ] **✅ Check**: `commonMain` does NOT reference Android/iOS APIs directly.
- [ ] **✅ Check**: Dependency inversion is used (Interface + DI) or `expect/actual`.
- [ ] **❌ Red Flag**: `Context`, `Activity`, `UIKit` appearing in `commonMain`.

### Logic Layering

- [ ] **Domain Layer**: Pure business logic, UseCases, Entities (100% testable). NO platform dependencies.
- [ ] **Data Layer**: Repositories, DTOs, Mappers. DOES NOT return platform types (e.g., `Cursor`).
- [ ] **Presentation Layer**: State / Intent / Reducer.

### expect/actual Usage

- [ ] **✅ Valid**: Time, File I/O, Key-Value Storage, Logger, Crypto, Device Info.
- [ ] **❌ Invalid**: Wrapping entire platform SDKs or duplicating complex logic.

## 2. Shared Code Quality (commonMain)

### Pure Kotlin Thinking

- [ ] **✅ Check**: No platform side-effects. Code runs in JVM/JS test environment.
- [ ] **❌ Red Flag**: usage of `java.*` or `android.*` packages.

### Error Handling

- [ ] **✅ Check**: Unified error model (e.g., `sealed class`, `Result`, `Either`).
- [ ] **❌ Red Flag**: Throwing arbitrary exceptions (Android `Exception` vs iOS `NSError`).

### Models

- [ ] **✅ Check**: DTOs separate from Domain Models.
- [ ] **✅ Check**: `kotlinx.serialization` is restricted to the Data layer.

## 3. Concurrency (Coroutines / Flow)

### Dispatchers

- [ ] **✅ Check**: Use `DispatcherProvider` (injected).
- [ ] **❌ Red Flag**: Hardcoded `Dispatchers.Main` or `Dispatchers.IO` in shared code.

### Lifecycle

- [ ] **✅ Check**: No scope leaks (all scopes cancelable).
- [ ] **✅ Check**: Shared ViewModels do not hold platform lifecycle objects.

## 4. Platform Implementation (Android/iOS actual)

### actual Implementations

- [ ] **✅ Check**: Thin wrappers only. No heavy business logic.
- [ ] **❌ Red Flag**: `actual` implementation diverges significantly between platforms.

### Dependency Injection

- [ ] **✅ Check**: Consistent abstraction used for injection on both platforms.

## 5. Testability

### commonTest

- [ ] **✅ Check**: Tests exist for UseCases, Reducers/ViewModels, Repositories.
- [ ] **❌ Red Flag**: Testing only present in `androidTest`; `commonTest` is empty.

### Design for Testing

- [ ] **✅ Check**: Dependencies (Clock, Logger, Dispatcher) are injectable, not singletons.

## 6. Build & Dependencies

### Gradle

- [ ] **✅ Check**: `commonMain` does not depend on `androidMain`.
- [ ] **✅ Check**: No platform libraries leaking into shared scope.

### API Surface

- [ ] **✅ Check**: Internal classes are marked `internal`. Only necessary API exposed.
