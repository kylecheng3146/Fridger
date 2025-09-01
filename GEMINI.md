# 📘 Gemini.md — Fridger 開發規範文件

## 🔧 專項技術一覽

| 類別   | 技術                                          |
| ---- | ------------------------------------------- |
| 語言   | Kotlin（Multiplatform）                       |
| UI   | Compose Multiplatform                       |
| 架構   | MVVM / 關注點分離 (Separation of Concerns)       |
| 資料庫  | SQLDelight                                  |
| 設定儲存 | DataStore                                   |
| 构建工具 | Gradle (Kotlin DSL, Version Catalog)        |
| 支援平臺 | Android / iOS / Desktop / WebAssembly（WASM） |

---

## 📁 專項目錄結構說明

```plaintext
composeApp/
├— src/
│   ├— commonMain/        <-- 共用程式碼（UI, ViewModel, Repository 等）
│   ├— androidMain/       <-- Android 平臺實作
│   ├— iosMain/           <-- iOS 平臺實作
│   ├— desktopMain/       <-- Desktop 平臺實作
│   └— wasmJsMain/        <-- WebAssembly 實作
└— build.gradle.kts
```

---

## 🧱 平面架構規範

### 1️⃣ Data Layer - 數據層

| 模組                | 說明                               |
| ----------------- | -------------------------------- |
| `data/database`   | SQLDelight 設定與 schema 檔案（`.sq`）  |
| `data/local`      | 本地資料存取實作                         |
| `data/model`      | 資料模型定義（例如 Ingredient，Category 等） |
| `data/repository` | 抽調出統一資料存取介面，處理進階與多資料源整合          |
| `data/settings`   | 使用 DataStore 管理設定資料              |

---

## 💪 風格與實作權則（KMP 實務檢查清單）

### 🔹 Null 安全與判空（跨平臺共用邏輯）

- ✅ 共用程式碼（commonMain）是否完全 Null-safe？是否避免 Nullable 傳查至 UI 層？
- ✅ 是否完全避免使用 `!!` 強制 unwrap（尤其 platform API）？
- ✅ 外部資料來源是否進行適當判空與格式驗證？
- 📌 expect/actual API 若添加 Nullable 傳入/傳出，平臺實作層需保證不造成 Null crash。

### 🔹 平臺生命週期管理

**Android**

- ✅ 是否使用 `viewModelScope` / `lifecycleScope` 控制 coroutine 生命週期？
- ✅ 避免在 `onCreateView()` 之前或 `onDestroyView()` 之後更新 UI？
- ✅ 是否考慮 `Configuration Change`（方向轉換）？

**iOS**

- ✅ Swift 與 ViewController 是否如此管理記憶體與生命週期？
- ✅ 異步任務是否避免在畫面未準備時更新 UI？
- ✅ Swift interop 是否穩定處理 nullable/optional？

### 🔹 Kotlin 語言特性應用

- ✅ 是否善用 `let` / `run` / `apply` / `also` 提升可讀性？
- ✅ 是否使用 `sealed class` 管理狀態（如 `UiState`, `ApiResult`）？
- ✅ 是否使用 `data class` 管理資料模型？
- ✅ 是否過度使用高階函式或巷層 lambda？降低 Swift side 可讀性？
- ✅ Extension function 僅限於平臺中立邏輯？避免延伸 iOS 專屬行為？

### 🔹 非同步與協組 (Coroutine / Flow)

- ✅ coroutine 是否明確指定 dispatcher？
- ✅ suspend / Flow API 是否封裝在共用層？
- ✅ coroutine 是否有 cancel / timeout 控制？
- ✅ try-catch 是否正確包裝 suspend 函式？
- ✅ Android: 使用 `viewModelScope`？
- ✅ iOS: 使用 `MainScope()` + `rememberCoroutineScope()`？

### 🔹 資料與邊界驗證

- ✅ API 回傳資料是否進行空值、欄位缺殖檢查？
- ✅ List/Array 使用是否先 `.isEmpty()` 或 `index in list.indices`？
- ✅ kotlinx.serialization 是否有處理 nullable/預設值？
- ✅ 資料庫/偏好設定讀取是否進行容錯與邏輯防告？

### 🔹 UI 操作與效能

**Android**

- ✅ 是否加入點擊 debounce / throttle 防重複提交？
- ✅ RecyclerView 、ViewPager 、LazyColumn 是否正確釋放與回收資源？
- ✅ 是否使用 `DiffUtil.ItemCallback` 或 `LazyColumn` diffing？
- ✅ 是否清除 ViewBinding / 動畫 callback？

**iOS**

- ✅ SwiftUI / UIViewController 是否與 Coroutine 安全互動？
- ✅ 是否避免在非主線線更新畫面？

### 🔹 資源與建置一致性

- ✅ 是否抽離所有 hardcoded 字串 / 顏色 / 尺寸至 resource 或 constant？
- ✅ 所有共用常數是否集中？
- ✅ 是否使用 `libs.versions.toml` 統一管理所有版本？

### 🔹 測試與傷錯除錯

- ✅ 共用邏輯是否具備單元測試（kotlin.test, expect mock）？
- ✅ Log 訊息是否避免漏露機故資訊？
- ✅ 是否包裝並格式化平臺錯誤堆疊與錯誤轉譯？

### 🔹 KMP 專用建議

- ✅ 共用與平臺邏輯是否明確分層？（不混用 Android / iOS API）
- ✅ 是否善用 Kotlin/Native 產生 `.framework` 並正確與 SwiftBridge 整合？
- ✅ 是否注意 Kotlin/Native concurrency model（避免共用 mutable state）？
- ✅ 是否監控產出 `.framework` 體積與啟動成本？

---

📌 本開發規範目的在於：

1. 維持統一的程式風格與架構邏輯
2. 促進跨平臺團隊協作與程式碼可維護性
3. 強化模組分工與測試專向開發精神

開發人員應遵循本規範進行實作，

---

資料傳輸規範 (Data Transfer Object Convention)

禁止事項
•	❌ 嚴禁 在 API 或資料庫序列化/反序列化中使用以下型別：
•	Map<String, Any>
•	Map<String, String>
•	org.json.JSONObject
•	任何其他「無結構的」字典/物件型別

理由：
•	失去型別安全，容易發生 runtime 錯誤。
•	無法被 Kotlin compiler/IDE 提示或檢查。
•	對前後端的契約（Contract）沒有清晰文件化，導致溝通成本上升。

⸻

強制規範
1.	所有 DTO / 資料模型必須使用 Kotlin data class 定義。
2.	必須依照使用的序列化工具，標記正確的註解：
•	Kotlinx Serialization → @Serializable
•	Gson → @SerializedName("field_name")
•	Jackson → @JsonProperty("field_name")
3.	所有 API 回應必須包裝在 ApiResponse<T> 中，保持一致結構

---

### 通用開發原則 (General Development Principles)

4. **禁止任何形式的硬編碼 (Hardcode)**：
   - **❌ 嚴禁**：在程式碼中直接寫入任何未經抽離的常數，特別是：
     - **字串 (Strings)**：如 API 端點 URL、錯誤訊息、UI 顯示文字。應使用常數、資源檔或設定檔管理。
     - **數字 (Numbers)**：如分頁大小、超時時間、重試次數。應定義為具名常數。
     - **金鑰與憑證 (Keys & Credentials)**：絕對禁止！必須透過環境變數、安全的設定檔或 Secret Management 服務載入。
