package its.model.definition.loqi

/**
 * Вспомогательные функции для замены специальных символов в строках
 */
internal object EscapeSequenceUtils {
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