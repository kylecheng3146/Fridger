# 架構與模組

## 模組切分
- shared:domain（UseCase / entity / sealed state）
- shared:data（repository / network / serialization）
- shared:common（dispatcher provider / logger）
- androidApp / iosApp（UI / DI / platform adapter）

## expect/actual
- Secure storage
- Network engine
- Analytics

## 依賴關係
- domain 不依賴 data
- data 依賴 domain + platform adapter
- UI 只依賴 use case
