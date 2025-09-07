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

5. 避免使用魔法值 (Avoid Magic Values)：
  - ❌ 嚴禁: 在業務邏輯、資料查詢或設定中，直接使用缺乏上下文的數值或字串，這些被稱
    為「魔法值」(Magic Values)。
    - 範例 (Bad): items.filter { it.daysUntilExpiry in 2..7 }
    - 範例 (Bad): if (user.role == 3)

  - ✅ 必須: 將這些值定義為具名常數 (named constants)，以提供清晰的業務含義。
    - 範例 (Good): private const val DAYS_RANGE_NEARING_EXPIRATION = 2..7
    - 範例 (Good): if (user.role == UserRole.ADMIN)

  - ✅ 建議: 對於跨越多個檔案或代表核心業務領域的常數，應將其集中在一個或多個專用的
    Constants.kt 檔案中進行管理（例如 DomainConstants.kt, ApiConstants.kt）。

  - 理由:
    - 可讀性: if (user.role == UserRole.ADMIN) 遠比 if (user.role == 3)
      更容易理解。
    - 可維護性: 當需要調整數值時（例如，即將到期的定義從 7 天改為 5 天），只需在
      Constants.kt 中修改常數定義，無需在整個專案中搜索和替換數值。

---

### 🧪 單元測試規範 (Unit Testing Convention)

#### 核心原則 (Core Principles)
- **隔離 (Isolation)**: 測試案例必須彼此獨立，且與外部系統 (如網路、真實資料庫) 隔離。
- **快速 (Fast)**: 單元測試的執行速度必須要快，以鼓勵頻繁執行。
- **可重複 (Repeatable)**: 無論執行幾次，測試結果都必須保持一致。
- **清晰 (Clear)**: 測試的意圖應一目了然，測試程式碼本身也需要具備高可讀性。

#### 測試工具 (Testing Tools)
| 工具 | 用途 |
|---|---|
| `kotlin.test` | 官方斷言庫，用於驗證測試結果 (如 `assertEquals`, `assertTrue`)。 |
| `io.mockk:mockk` | 建立 Mock 物件的標準函式庫，用於隔離測試單元與其外部依賴。 |
| `com.h2database:h2` | 記憶體資料庫，用於在測試環境中模擬真實資料庫，以驗證資料層邏輯。 |

#### 各層級測試策略 (Testing Strategy by Layer)

- **Data Layer (Repositories)**
  - **策略**: 使用 H2 記憶體資料庫進行測試。
  - **實作**: 每個測試類別需在測試前建立資料庫 Schema，並在測試後清理，確保測試的獨立性。
  - **重點**: 驗證 SQL 查詢的正確性，包含資料的增、刪、改、查與對應關係。
  - **範例**: `UserRepositoryTest` 應驗證 `findOrCreateUserByGoogle` 是否能正確地新增或查詢使用者。

- **Service Layer (Services)**
  - **策略**: 使用 MockK 模擬 (Mock) 所有外部依賴 (如 Repositories, Validators)。
  - **實作**: 將 Mock 物件注入到 Service 中，設定其預期行為與回傳值。
  - **重點**: 專注於測試 Service 本身的商業邏輯，驗證其是否根據依賴的回傳值做出正確的判斷與處理。
  - **範例**: `AuthServiceTest` 應模擬 `UserRepository` 來測試登入與 Token 刷新邏輯。

- **Security Layer (Validators)**
  - **策略**: 避免真實的網路或複雜的密碼學運算，專注於邏輯驗證。
  - **實作**: 對於如 `GoogleTokenValidator` 的元件，應手動建立測試用的 JWT 與公鑰。
  - **重點**: 將測試用的公鑰注入 Validator，驗證其解析、簽名驗證、過期判斷等邏輯是否正確。

- **Routing Layer (Ktor Routes)**
  - **說明**: 路由層通常涉及多個元件的整合，建議使用 Ktor 提供的 `testApplication` 進行**整合測試 (Integration Test)**，而非單元測試。這部分不在單元測試的範疇，但為測試策略的下一步。

#### 命名與結構 (Naming and Structure)
- **檔案命名**: 測試檔案應以被測試的類別命名，並加上 `Test` 後綴。 (例如: `AuthService.kt` → `AuthServiceTest.kt`)
- **函式命名**: 測試函式應清楚描述其測試的情境與預期結果，可使用反引號 (``) 增強可讀性。 (例如: `fun `given invalid token, refreshAccessToken should throw exception``)

📝 UI 與 ViewModel 開發規範 (UI & ViewModel Development Convention)

1. 資源管理 (Resource Management)

- 原則: UI 層不應存在任何未被管理的「魔法值」(Magic Values)。
- 實踐清單:
  - ✅ 字串資源化 (String Externalization)
    - ❌ 嚴禁: 在 Composable 或 ViewModel 中直接使用硬編碼字串 (e.g.,
      "新增食材", "過期日")。
    - ✅ 必須: 將所有面向使用者的字串定義在
      composeApp/src/commonMain/composeResources/values/strings.xml 中。
    - ✅ 必須: 在程式碼中透過 stringResource(Res.string.id) 引用字串。
    - 理由: 便於統一管理、修改，並為未來多語言支援 (i18n) 奠定基礎。
  - ✅ 顏色與尺寸規格化 (Color & Sizing Standardization)
    - ❌ 嚴禁: 在 Modifier 中使用 color = Color(0xFF...) 或 padding = 16.dp
      等未定義的數值。
    - ✅ 必須: 優先使用 MaterialTheme.colorScheme
      中的顏色。若需自訂，應定義在統一的 Theme.kt 檔案中。
    - ✅ 必須: 將常用的 dp 或 sp 數值定義在 Theme.kt 的擴充屬性中 (e.g.,
      MaterialTheme.spacing.medium)。
    - 理由: 確保 UI 風格一致，並易於進行全域性的主題調整。

2. ViewModel 設計原則 (ViewModel Design Principles)

- 原則: ViewModel 應保持精簡，專注於「狀態管理」與「業務邏輯」，而非資料轉換的細節。
- 實踐清單:
  - ✅ 職責單一與邏輯抽取 (Single Responsibility & Logic Extraction)
    - ❌ 避免: 在 ViewModel 中撰寫複雜、與核心業務無關的輔助邏輯 (e.g.,
      基於名稱決定圖示的長篇 when 判斷式)。
    - ✅ 應該: 將可獨立的、純粹的資料轉換或對應邏輯，抽取至獨立的輔助類別
      (Helper/Mapper Class)，例如 IngredientIconMapper。
    - 理由: 保持 ViewModel 的簡潔，專注於處理 UI 狀態 (State) 與業務流程
      (Flow)，並提高輔助邏輯的可重用性與可測試性。
  - ✅ 函式簡潔化 (Function Simplification)
    - ❌ 避免: 撰寫單一巨大函式來處理多個步驟的資料載入與轉換 (e.g.,
      一個函式內同時處理 API 請求、資料庫讀取、多重 map 操作、排序、分組)。
    - ✅ 應該: 將複雜的資料處理流程，拆分為數個職責單一的私有函式 (e.g., private
      fun mapToUiModels(), private fun groupAndSortItems())。
    - 理由: 提升程式碼的可讀性、可維護性，並使邏輯單元更易於獨立理解與測試。

---

🧪 單元測試規範 (Unit Testing Convention)

核心原則 (Core Principles)
- 隔離 (Isolation): 測試案例必須彼此獨立，且與外部系統 (如網路、真實資料庫) 隔離。
- 快速 (Fast): 單元測試的執行速度必須要快，以鼓勵頻繁執行。
- 可重複 (Repeatable): 無論執行幾次，測試結果都必須保持一致。
- 清晰 (Clear): 測試的意圖應一目了然，測試程式碼本身也需要具備高可讀性。

測試工具 (Testing Tools)

┌────────────────────────────────┬───────────────────────────────────────────┐
│ 工具                           │ 用途                                      │
├────────────────────────────────┼───────────────────────────────────────────┤
│ kotlin.test                    │ 官方斷言庫，用於驗證測試結果 (如 `asse... │
  │ io.mockk:mockk                 │ 建立 Mock 物件的標準函式庫，用於隔離測... │
  │ `org.jetbrains.kotlinx:kotl... │ 提供測試 Coroutines 的工具，如 runTest... │
└────────────────────────────────┴───────────────────────────────────────────┘


各層級測試策略 (Testing Strategy by Layer)
- Data Layer (Repositories)
  - 策略: 應在整合測試 (Integration Test) 中使用記憶體資料庫 (如 H2)
    或真實的測試資料庫進行驗證，單元測試的範疇較小。
- ViewModel/Service Layer
  - 策略: 使用 MockK 模擬 (Mock) 所有外部依賴 (如 Repositories, Mappers)。
  - 實作: 將 Mock 物件注入到被測單元中，設定其預期行為與回傳值。
  - 重點: 專注於測試自身的商業邏輯，驗證其是否根據依賴的回傳值做出正確的判斷與處理。
- Mappers/Validators
  - 策略:
    這些通常是純函式或純邏輯元件，應直接進行測試，驗證輸入與輸出的對應關係是否正確。

  ---
實作細則與檢查清單 (Implementation Details & Checklist)

- ✅ 檔案與函式命名 (Naming Convention)
  - 檔案: 測試檔案應以被測試的類別命名，並加上 Test 後綴。
    - 範例: HomeViewModel.kt → HomeViewModelTest.kt
  - 函式: 測試函式應使用反引號 (`) 包裝，並清楚描述其測試的情境與預期結果，可遵循 
         given[情境]_when[動作]_then[預期結果]` 的結構。
    - 範例: fun \given repository returns empty list, when loadHomeData is
      called, then uiState is empty\``
    - 理由: 清晰的命名讓測試失敗時能立刻了解是哪個環節出錯。

- ✅ Mock 的使用 (Usage of Mocks)
  - 必須: 使用 io.mockk:mockk 來模擬所有外部依賴。
  - 必須: 在 @BeforeTest (或每個測試函式開頭) 初始化 Mocks，並使用 relaxUnitFun =
    true 或 relaxed = true 來簡化不需要回傳值的 Mock 設定。
  - 必須: 使用 every { ... } returns ... 或 coEvery { ... } returns ... (針對
    suspend 函式) 來定義 Mock 物件的行為。
  - ❌ 嚴禁: 在單元測試中實例化真正的 Repository 或 Service。
    - 理由: 確保測試的隔離性，只專注於被測試單元的邏輯。

- ✅ 斷言與驗證 (Assertions & Verification)
  - 必須: 使用 kotlin.test 中的斷言函式 (assertEquals, assertTrue, assertIs 等)
    來驗證最終結果。
  - 建議: 在測試結束時，可使用 verify { ... } 或 coVerify { ... } (MockK)
    來驗證特定的函式是否被如預期般呼叫。
    - 理由: 斷言是測試的核心，它驗證了程式碼的行為是否符合預期。

- ✅ ViewModel 與 Coroutine 測試 (ViewModel & Coroutine Testing)
  - 必須: 測試 ViewModel 時，應專注於驗證 UiState 的變化是否符合預期。
  - 必須: 由於 ViewModel 中的協程是非同步的，測試函式必須使用
    kotlinx-coroutines-test 提供的 runTest 來執行。
  - 必須: 在測試類別中建立一個 TestDispatcher，並在 @BeforeTest 中透過
    Dispatchers.setMain(testDispatcher) 將其設定為 Main Dispatcher，在 @AfterTest
    中 Dispatchers.resetMain()。
    - 理由: 確保能以可控制、同步的方式測試與 viewModelScope (Dispatchers.Main)
      相關的非同步邏輯。
