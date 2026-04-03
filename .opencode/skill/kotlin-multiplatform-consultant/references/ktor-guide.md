## Ktor 使用準則

### Client

- 使用 multiplatform engine
- 錯誤處理集中在 data 層
- HTTP timeout 與 retry 策略須定義

### Server (選用)

- 只在確定需要共享 server stack 時採用
- 需列出 routing / auth / serialization 設計
