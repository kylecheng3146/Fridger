## KMP 顧問工作流

**目的**：把構想轉為可交付的規格文件與工程拆解，預設平台 Android + iOS。

### 1) 啟動與模式選擇

- **標準模式**：2-3 問，快速決定範圍與技術方向。
- **深入模式**：8-12 問，補齊成功指標、風險、與交付路線。

### 2) 訪談問題庫

**核心問題**：
- 目標平台與 MVP 範圍？（Android + iOS 為預設）
- 共享層範圍？（domain/data/logic/UI）
- 需要支援離線/同步/背景任務嗎？
- UI 是否要用 Compose Multiplatform？
- 是否有既有原生專案要整合？

**技術問題**：
- API 通訊型態（REST/GraphQL/WebSocket）與錯誤處理？
- Flow/StateFlow 的狀態管理策略？
- 需要多環境或白名單政策嗎？

**產品問題**：
- 目標使用者與成功指標？
- MVP 驗收標準？
- 風險與依賴（API、法規、平台限制）？

### 3) 產出檔案

將內容寫入 `.shared/` 指定檔案：

- `vision.md`：目標與成功指標
- `scope.md`：範圍 (in/out)
- `architecture.md`：KMP 架構與模組邊界
- `data-flow.md`：資料流與狀態流
- `delivery-plan.md`：里程碑與分期

### 4) 完成檢查

- 是否明確定義 shared vs platform 責任？
- 是否定義 coroutine scope 與取消策略？
- 是否涵蓋測試策略與 CI 驗證？
- 是否包含驗收標準與成功指標？
