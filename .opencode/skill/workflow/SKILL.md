---
name: workflow
description: "Kotlin Multiplatform 的端到端 Conductor 工作流：產品規劃與規格（SDD）然後 測試計畫 然後 先寫測試（TDD）然後 實作 然後 Gradle 驗證 然後 自我代碼審查 然後 依嚴格範本提交 git commit。使用 work_id 格式 YYYYMMDD-TYPE-SCOPE-SLUG，並把文件輸出到 docs/WORK_ID/。"
---

# 工作流：SDD + TDD

## 作業規則
- 在改任何程式碼前先完成 SDD。
- 優先採用 TDD：可行時先寫會失敗的測試。
- 小步提交、小步驗證、保持可讀性。
- 每個階段結束都要產出明確工件（文字輸出與／或檔案變更）。
- 未通過驗證或未完成審查清單，不得提交 commit。
- 需求不清楚時，最多問 3 個釐清問題；其後以「明確假設」繼續推進。

## Phase 0 — 需求收斂與限制
- 蒐集：目標／user story、限制條件、目標平台、風險。
- 輸出（每行都以 `-` 開頭）：
- Assumptions：清單
- Risks：清單
- Plan：5–8 步，且每步以 `-` 開頭

## Phase 1 — SDD（Spec-Driven Design）
- 產出規格文件，且所有行都以 `-` 開頭：
- Problem
- Users
- User stories
- Acceptance criteria（使用 `AC-01`, `AC-02`...）
- API/Contracts（資料模型、函式簽名）
- Edge cases
- Observability
- Rollout/相容性注意事項

### SDD/TestPlan 工件（落地到 Repo）
- Docs 根目錄：`docs/`
- 推導 `work_id`：`YYYYMMDD-TYPE-SCOPE-SLUG`
- 建立資料夾：`docs/WORK_ID/`
- 寫入：
- `docs/WORK_ID/00-sdd.md`
- `docs/WORK_ID/01-test-plan.md`
- `docs/WORK_ID/02-verification.md`

### 可追溯性規則
- `00-sdd.md` 必須連到 `01-test-plan.md` 與 `02-verification.md`。
- 測試案例使用 `TC-01`, `TC-02`...，且每個 TC 必須標註覆蓋哪些 `AC-xx`。

## Phase 2 — 測試計畫
- 建立對應 Acceptance Criteria 的測試矩陣：
- Unit tests（shared logic 優先放在 `commonTest`）
- Integration tests（serialization／network／storage 邊界）
- Platform tests（androidTest／iOS）僅在必要時加入
- Regression scope（可能受影響的 modules/features）
- Test data 策略（fixtures/builders）

## Phase 3 — TDD（先寫測試）
- 偵測既有測試框架並遵循專案慣例。
- 先寫會失敗的測試（red），再實作到通過（green），最後重構（refactor）。
- 輸出：
- Tests added：檔案清單 + 目的
- Expected failures（實作前預期會失敗的點）

## Phase 4 — 實作
- 只實作足以滿足測試的最小變更。
- 維持 KMP 分層清晰：
- 盡量把 domain logic 放在 `commonMain`。
- 平台差異使用 `expect/actual` 或注入介面隔離。
- 輸出：
- Implementation summary
- Files changed

## Phase 5 — 工具／任務偵測（KMP）
- 偵測建置結構：
- 是否存在 `./gradlew`、`settings.gradle(.kts)`、`build.gradle(.kts)`。
- 從 `settings.gradle(.kts)` 的 `include(...)` 推導 modules。
- 透過 `build.gradle(.kts)`／version catalog／設定檔偵測 lint/test：
- detekt：`io.gitlab.arturbosch.detekt` 或 `detekt.yml`
- ktlint：`org.jlleitschuh.gradle.ktlint`
- spotless：`com.diffplug.spotless`
- kotest：dependencies 含 `io.kotest`

## Phase 6 — 驗證（Gradle）
- 先跑最小集合，再視情況擴大。

### Task 探勘（不確定時）
- 不要猜 task 名稱，先列出：
- `./gradlew tasks --all`
- 篩選測試：`./gradlew tasks --all | rg -i "test|allTests|check"`
- 篩選 lint：`./gradlew tasks --all | rg -i "detekt|ktlint|spotless"`

### 最小驗證集合（常見）
- 能鎖定 module 時先跑：`./gradlew :MODULE:test`
- shared KMP 測試（若有配置）：`./gradlew :MODULE:allTests` 或 `:jvmTest`

### Lint／靜態分析（僅在存在時）
- detekt：`./gradlew detekt` 或 `./gradlew :MODULE:detekt`
- ktlint：`./gradlew ktlintCheck` 或 `./gradlew :MODULE:ktlintCheck`
- spotless：`./gradlew spotlessCheck`

### 輸出（同時寫到 `docs/WORK_ID/02-verification.md`）
- Commands run（每行以 `-` 開頭）
- Results summary
- Manual verification steps（若環境限制導致無法執行某些步驟）

## Phase 7 — 自我代碼審查
- 審查清單（每行以 `-` 開頭）：
- Correctness（對齊 acceptance criteria）
- Tests（品質與覆蓋）
- KMP boundaries（common vs platform）
- Readability（命名與結構）
- Error handling（含 nullability）
- Performance（避免明顯退化）
- Security/privacy（避免輸出敏感資訊）
- Public API 變更是否有同步在 SDD

- 並輸出：
- Diff summary（5–10 點）

## Phase 8 — Git Commit（嚴格範本）
- 只有在驗證成功後才可以 commit。
- commit 的 scope 必須與 `work_id` 的 scope 一致。

### Commit Message Format
- `feat(xxxx): 功能說明`
- 空行
- `摘要:`
- 空行
- `{{詳細說明改動項目}}`
- 空行
- `主要變更內容：`
- 空行
- `{{詳細內容}}`
- 空行
- `影響範圍：`
- 空行
- `{{影響範圍}}`

### Type 規則
- `TYPE` 必須是：`feat`, `fix`, `docs`, `refactor`, `test`, `chore`。

## 最終輸出契約（每次都要有）
- SDD:
- Test plan:
- Tests（TDD）:
- Implementation:
- Verification:
- Code review:
- Commit message:
