## KMP 架構建議

### 模組邊界

- **shared:domain**
  - 業務規則、UseCase、entity、sealed class 狀態
- **shared:data**
  - repository 介面與實作、資料來源、序列化模型
- **shared:common**
  - logger、時間、Dispatcher provider、結果封裝
- **androidApp / iosApp**
  - 平台 UI、依賴注入、平台 API 介接

### expect/actual 策略

將平台 API 隔離到 `expect/actual`：

- Filesystem
- Secure storage
- Network client platform engine
- Analytics / Crash reporting

### 分層規範

- domain 不得依賴 data
- data 只能依賴 domain + platform adapter
- UI 層只接收 use case 結果，不直接操作 data

### 交付物

- 模組依賴圖
- 平台責任矩陣 (shared / android / ios)
- 可替換的依賴注入設計
