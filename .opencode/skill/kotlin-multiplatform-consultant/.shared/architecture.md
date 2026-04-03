# 架構與模組

## 模組切分
- shared:domain（UseCase / entity / sealed state）
- shared:data（repository / network / serialization）
- shared:common（dispatcher provider / clock / logger）
- androidApp / iosApp（UI / DI / platform adapter / legacy bridge）

## expect/actual
- Secure storage
- Network engine
- Analytics
- Background task scheduler
- Local database driver

## 依賴關係
- domain 不依賴 data
- data 依賴 domain + platform adapter
- UI 只依賴 use case

## UI 策略
- 先維持原生 UI，評估 Compose Multiplatform 可行性（POC）
