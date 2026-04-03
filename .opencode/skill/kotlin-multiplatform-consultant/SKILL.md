---
name: kotlin-multiplatform-consultant
description: Use when需要進行 Kotlin Multiplatform (KMP) 顧問式規劃、將 KMP 構想轉為規格文件、定義共享/平台層邊界、或涉及 coroutine/Flow、Compose Multiplatform、Ktor、Gradle Kotlin DSL 的跨平台架構決策時。
---

# Kotlin Multiplatform 顧問

以顧問流程將 KMP 構想轉為可交付規格與工程執行藍圖，預設平台為 Android + iOS。

## 技術堆疊 (Tech Stack)

- **語言/版本**: Kotlin 1.9+
- **平台**: Android + iOS
- **架構**: KMP shared + platform modules
- **非同步**: Coroutines + Flow
- **UI**: Compose Multiplatform (可選)
- **網路**: Ktor Client / Server (視需求)
- **建置**: Gradle Kotlin DSL
- **測試**: kotlinx-coroutines-test + Turbine + 平台測試

## 適用時機 (When to Use)

**請在以下情況使用：**
- 規劃或審視 Kotlin Multiplatform 架構
- 需要跨平台共用 domain/data 層
- 需要設計 coroutine/Flow 生命週期與取消策略
- 需要評估 Compose Multiplatform 或 Ktor 的可行性
- 需要產出 KMP 規格書與工程拆解

**請勿在以下情況使用：**
- 單一平台純 Kotlin 專案
- 只有 UI 樣式調整且無跨平台需求
- 明確排除 KMP 的專案

## 啟動工作流 (Starting the Workflow)

當使用者呼叫此技能時：

1. **詢問訪談模式 (Ask Interviewer Mode)**
   - 標準模式 (Standard): 2-3 問，5-8 分鐘
   - 深入模式 (Deep): 8-12 問，20-40 分鐘

2. **啟動選定的訪談者 (Launch Selected Interviewer)**
   依照 `references/workflow.md` 中定義的工作流進行。

3. **產出規格文件**
   將結果輸出到 `.shared/` 指定檔案（依 workflow 要求）。

## 代理委派格式 (Agent Delegation Format)

```
TASK: [具體目標]
EXPECTED OUTCOME: [輸出檔案]
REQUIRED AGENT: [工作流中的 Agent 名稱]
CONTEXT: [必要的輸入檔案]
```

## 關鍵規則 (Critical Rules)

**必須做 (MUST DO):**
- 閱讀 `.shared/` 中先前的輸出
- 遵循 Agent 指引並寫入指定檔案
- 遵守 KMP 分層與 expect/actual 邊界
- 使用結構化併發，正確處理取消
- 以 immutable data 與 sealed class 建模
- 產出明確的成功指標與驗收標準（參考 PM toolkit）

**絕對不可做 (MUST NOT DO):**
- 在 common 模組混入平台專屬 API
- 使用 `GlobalScope` 或阻塞式 `runBlocking` 於 production
- 隨意擴權或引入不必要的第三方依賴
- 將 UI/平台細節污染 domain 層

## 參考文件 (Reference Documentation)

- **工作流**: `references/workflow.md`
- **KMP 架構**: `references/kmp-architecture.md`
- **Coroutine/Flow**: `references/coroutines-flow.md`
- **Compose MPP**: `references/compose-mpp.md`
- **Ktor 指南**: `references/ktor-guide.md`
- **Gradle 設定**: `references/gradle-kmp.md`
- **測試策略**: `references/testing-kmp.md`
- **產品規格**: `references/pm-prd.md`
