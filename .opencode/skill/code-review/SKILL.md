---
name: code-review
description: Structured code review workflow that produces an actionable review report with severity triage (🔴 must fix, 🟡 strongly recommend, 🟢 optional). Use when reviewing PRs/patches/snippets, requesting a code quality assessment, or needing feedback across readability, efficiency, maintainability, and robustness (incl. error handling, input validation, security).
---

# Code Review

Provide a concise, high-signal code review with concrete fixes.

## Workflow

1. Read context first: goal, constraints, language/framework conventions, and what files/changes are in scope.
2. Identify issues and classify by severity:
   - 🔴 **嚴重問題（必須修復）**: correctness bugs, crashes, security, data loss, broken API/contract, major performance regressions.
   - 🟡 **重要問題（強烈建議）**: design flaws, maintainability risks, non-idiomatic patterns likely to cause bugs, missing tests/edge cases.
   - 🟢 **改進建議（可選）**: style/naming polish, minor refactors, small perf wins, readability enhancements.
3. Prefer high-value findings over noise:
   - Report only issues that could cause real problems.
   - Keep feedback concise and actionable.
   - Offer specific changes (what to change, where, and how).
4. If requirements are unclear, state assumptions explicitly in the report.
5. If tests are missing, assume reasonable requirements and assess correctness under those assumptions.

## Output Requirements

- Write in Traditional Chinese.
- Use headings and bullets; keep items actionable.
- For each issue, include: **Impact**, **Evidence** (reference file/line if provided), and **Fix** (concrete suggestion).
- Avoid subjective tone; base statements on observable facts.

## Review Report Format

### 🔴 嚴重問題（必須修復）
- {Issue 1}: Impact / Evidence / Fix
- {Issue 2}: Impact / Evidence / Fix

### 🟡 重要問題（強烈建議）
- {Issue 1}: Impact / Evidence / Fix
- {Issue 2}: Impact / Evidence / Fix

### 🟢 改進建議（可選）
- {Suggestion 1}: Value / Evidence / Change
- {Suggestion 2}: Value / Evidence / Change

---

#### 1. 可讀性
- 評論：{描述程式碼的命名、結構、註釋等是否清晰易懂}
- 改進建議：{具體建議}

#### 2. 效率
- 評論：{分析時間/空間複雜度，是否使用最佳演算法}
- 改進建議：{具體建議}

#### 3. 可維護性
- 評論：{評估模組化程度、設計原則、文件質量等}
- 改進建議：{具體建議}

#### 4. 穩健性
- 評論：{評估錯誤處理、輸入驗證、安全性等}
- 改進建議：{具體建議}

**總結**：
{整體評價：主要優點 + 最關鍵的改進重點 + 建議優先順序}

**注意事項**：
- 程式碼未提供測試用例時，請假設合理的功能需求並據此評估正確性。
- 請考慮該語言/框架的慣例與最佳實踐。
- 問題背景或需求不夠明確時，請在報告中註明假設。
- 保持客觀，避免主觀偏見，確保回饋基於事實和標準。
