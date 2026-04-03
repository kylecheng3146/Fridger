## Coroutines / Flow 指引

### 結構化併發

- 禁用 `GlobalScope`
- 使用 `coroutineScope` 或 `supervisorScope`
- 明確界定 scope 生命週期

### Dispatcher 策略

- 共享層預設 `Dispatchers.Default`
- Android 使用 `Dispatchers.Main` / `Dispatchers.IO`
- iOS 透過注入或 wrapper 處理 UI dispatcher

### Flow 使用原則

- UI 狀態使用 `StateFlow`
- 一次性事件使用 `SharedFlow`
- `stateIn` / `shareIn` 必須指定 scope

### 取消與清理

- 長迴圈呼叫 `ensureActive()`
- 關鍵區段使用 `try/finally` 清理
