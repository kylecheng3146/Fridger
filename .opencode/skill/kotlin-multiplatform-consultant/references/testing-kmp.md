## KMP 測試策略

### Shared 測試

- 使用 `kotlinx-coroutines-test`
- Flow 使用 Turbine 驗證

### 平台測試

- Android: JVM / instrumentation
- iOS: 封裝在 Swift 測試或 KMM 測試框架

### 驗收

- 產出測試覆蓋範圍
- 定義關鍵流程測試清單
