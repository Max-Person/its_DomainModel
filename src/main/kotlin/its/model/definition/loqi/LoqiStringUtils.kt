package its.model.definition.loqi

/**
 * Вспомогательные функции для работы со строками
 */
internal object LoqiStringUtils {
    /**
     * Список ключевых слов в LOQI
     */
    val keywords = LoqiGrammarLexer.VOCABULARY.let {
        //Предполагаем, что названия всех токенов в грамматике, длиной в 2 и больше символа - ключевые слова
        val maxTokenType = it.maxTokenType
        val keywords = mutableListOf<String>()
        for (i in 0..maxTokenType) {
            val tokenName = it.getLiteralName(i)
            if (tokenName.isNullOrBlank() || tokenName.length < 2) continue
            keywords.add(tokenName.removeSurrounding("'"))
        }
        keywords as List<String>
    }

    fun String.isSimpleLoqiName(): Boolean {
        return this.matches("[a-zA-Z\$_][a-zA-Z0-9\$_]*".toRegex()) && !keywords.contains(this)
    }


    val knownSequences = listOf(
        "\\" to "\\\\", //Важно что просто слеши заменяются первыми
        "\b" to "\\b",
        "\t" to "\\t",
        "\n" to "\\n",
        "\r" to "\\r",
        "\'" to "\\'",
        "\"" to "\\\"",
    )

    fun String.extractEscapes(): String {
        var out = this
        for (sequencePair in knownSequences) {
            out = out.replace(sequencePair.second, sequencePair.first)
        }
        return out
    }

    fun String.insertEscapes(): String {
        var out = this
        for (sequencePair in knownSequences) {
            out = out.replace(sequencePair.first, sequencePair.second)
        }
        return out
    }
}