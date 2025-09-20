package fridger.com.domain.translator

interface Translator {
    suspend fun translate(text: String, sourceLang: String = "en", targetLang: String = "zh-TW"): String
}

class MockTranslator : Translator {
    override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        // 在真實翻譯服務接入前，先簡單加上後綴以供識別
        return "$text [譯]"
    }
}
