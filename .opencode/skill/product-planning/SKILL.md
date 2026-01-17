---
name: product-planning
description: 產品經理/產品規劃工作流與產出模板（PRD）。用於把一句話需求或商業問題，系統化收斂成：問題定義、目標與指標、使用者與情境、範圍（in/out）、解法方案比較、MVP/迭代拆解、需求明細與驗收標準、埋點/實驗設計、上線/回滾計畫、風險與依賴。適用於 0→1 新功能規劃、成長/轉換漏斗優化、需求評審與對齊、跨團隊交付前的 PRD 寫作與補完。
---

# 產品規劃

使用此技能時，目標是「不要憑空補完」：缺資訊先提問、把假設標出來、把可驗證的標準寫清楚。

## 工作流選擇（0→1 vs 成長）

- **0→1 新功能**：從使用者問題與情境出發，強調流程、權限/狀態、例外處理、非功能需求（效能/隱私/成本）。
- **成長/優化**：從漏斗與行為出發，強調基準線、實驗設計（A/B）、量測窗、guardrails、逐步上線策略。

若未指定，預設以「0→1」為主，並補齊成長視角的指標與埋點。

## 需求收斂（先問再寫）

在開始輸出 PRD 前，先用下列問題收斂；缺的資訊要追問，不能直接猜。

1. **一句話需求**：要解決什麼問題？為什麼現在要做？
2. **目標使用者**：誰會用？主要使用情境？（B2C/B2B、角色/權限）
3. **成功指標**：North Star 是什麼？目標值？量測窗？
4. **Guardrails**：不可犧牲什麼？（錯誤率、退款、延遲、客服量、合規）
5. **基準線/現況**：目前數字是多少？（若未知，列出需要補的數據）
6. **範圍與不做**：MVP 必做/不做，各有哪些？
7. **限制**：時程、人力、平台、法規/隱私、相容性、技術債
8. **依賴**：需要哪些團隊/系統配合？
9. **使用流程**：理想流程、失敗/例外流程（含復原）
10. **資料與量測**：需要哪些事件/屬性？要看哪些報表？是否要 A/B？
11. **上線策略**：灰度、回滾、客服/營運準備、公告

## PRD 模板（輸出格式）

用 Markdown 輸出以下模板；若某節資訊不足，寫 `TBD`，並在「待確認問題」列出需要補的點。

### 1. 摘要（Summary）
- 背景/問題
- 目標
- 非目標（Out of scope）

### 2. 使用者與情境（Users & Use Cases）
- Persona/角色與權限
- 核心情境（Top 3）

### 3. 目標與指標（Goals & Metrics）
- North Star metric（定義、計算方式、目標值、量測窗）
- Supporting metrics
- Guardrails（風險指標）

### 4. 現況與洞察（Baseline & Insights）
- 現況流程/數據
- 問題根因假設（可驗證）

### 5. 範圍（Scope）
- In scope（MVP）
- Out of scope
- 里程碑（MVP → Iteration 1/2…）

### 6. 解法概述（Solution Overview）
- 使用者旅程與流程圖（文字描述即可）
- 主要狀態（state）與轉換

### 7. 需求明細（Functional Requirements）
以「需求編號 + 描述 + 驗收標準」書寫：
- FR-1 …
  - 描述：
  - 驗收標準（AC）：Given/When/Then 或可測條列

### 8. 例外與邊界（Edge Cases）
- 錯誤狀態、重試、離線/限流、權限不足

### 9. 非功能需求（NFR）
- 效能/延遲
- 可用性/容錯
- 隱私/資安/合規
- 可觀測性（logging/metrics）

### 10. 數據埋點與實驗（Analytics & Experimentation）
- 事件（event）清單：事件名、觸發時機、屬性（properties）
- 分析維度與報表
- A/B（若適用）：假設、受眾、指標、量測窗、停止規則

### 11. 上線計畫（Rollout Plan）
- 版本/平台
- 灰度策略
- 回滾條件與流程
- 溝通：公告、客服/營運/法務

### 12. 風險與依賴（Risks & Dependencies）
- RAID：Risks/Assumptions/Issues/Dependencies

### 13. 開放問題（Open Questions）
- Q1…

## 輸出規則（品質門檻）

- **先提問後輸出**：若關鍵欄位缺失（使用者/指標/範圍/限制），先提出最少必要問題。
- **AC 必須可測**：避免「好用/順暢」等不可驗證措辭。
- **指標必須可計算**：寫清楚定義與量測窗；同時給 guardrails。
- **清楚標示假設**：所有未證實資訊用「假設」標註。

## Integration（A：product-planning → ux-design）

目標：PRD 一寫完，立刻可交給 `ux-design` 產出 flow / wireframe / spec / test plan，且每條需求可追溯。

**交接必備欄位（PRD 內應明確寫出）**

- `Users & Use Cases`：角色、權限、主要情境
- `Goals & Metrics`：North Star、supporting、guardrails、量測窗
- `Functional Requirements`：每條有 `FR-#` 與可測 `AC`
- `Edge Cases`：錯誤、權限、離線、復原
- `Analytics & Experimentation`：事件與屬性（若適用）

**給 ux-design 的指令（可直接貼在 PRD 最後）**

- 「請使用 `ux-design`，針對 `FR-#` 逐條產出：`User flow`（Mermaid）、`Wireframe`（文字）、`UI Spec`（states/interactions/a11y/edge cases）、以及必要的 `Microcopy` 與 `Usability test plan`。所有輸出需保留 `FR-#` 對應（traceability）。」

**Handoff Pack（最小交付包）**

- PRD（含 `FR-#` / `AC` / `Edge Cases`）
- 一頁摘要：主要任務、成功指標、主要限制
- UX 交付：Flow / Wireframe / Spec / Test plan

## 使用範例（觸發語句）

- 「幫我把這句需求寫成 PRD：會員可以收藏商品，並在個人頁查看。」
- 「這個漏斗轉換掉 5%，幫我做成長版 PRD（含 A/B 與埋點）。」
- 「幫我評審這份 PRD，找風險、缺漏、指標問題。」
