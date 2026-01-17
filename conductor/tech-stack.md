# Tech Stack - Fridger

## 1. 核心開發語言與平台 (Core Language & Platform)
- **Kotlin (2.1.21)**：全專案主要開發語言。
- **Compose Multiplatform (1.8.1)**：用於建構跨平台 (Android, iOS, Desktop, Web) 的宣告式 UI。

## 2. 客戶端技術 (Client Side)
- **SQLDelight (2.0.2)**：本地資料庫持久化，確保跨平台類型安全。
- **DataStore (1.1.1)**：管理輕量級的使用者偏好設定。
- **Voyager (1.0.0)**：跨平台導航框架。
- **Coil 3 (3.0.4) / Kamel (1.0.0)**：非同步圖片載入與快取。

## 3. 後端技術 (Backend Side)
- **Ktor Server (3.0.2)**：高效能、非同步的伺服器框架。
- **Exposed (0.53.0)**：Kotlin 優先的 SQL ORM。
- **PostgreSQL (42.7.4)**：主要生產資料庫。
- **Flyway (10.17.0)**：資料庫版本控制與遷移管理。

## 4. 基礎設施與工具 (Infrastructure & Tools)
- **Kotlinx Coroutines (1.10.2)**：處理所有非同步任務與資料流。
- **Kotlinx Serialization**：統一的 JSON 編碼與解碼。
- **Kotlinx Datetime (0.6.1)**：跨平台日期與時間處理，支援到期日與儀表板統計計算。
- **Ktor Client**：用於客戶端與後端 API 通訊。

## 5. 測試與品質控管 (Testing & Quality)
- **MockK (1.13.12)**：單元測試的 Mock 框架。
- **Kotlin Test**：標準測試框架。
- **Kover (0.8.2)**：程式碼覆蓋率統計。
- **Ktlint**：Kotlin 程式碼風格檢查。
