# 資料流與狀態流

## Data Flow
- API -> repository -> use case -> UI
- Sync: 背景同步 + 本地快取

## StateFlow / SharedFlow
- UI 狀態以 StateFlow 發布
- 一次性事件以 SharedFlow 發布

## 取消與清理
- ViewModel scope 管理 UI
- long-running work 使用 ensureActive
