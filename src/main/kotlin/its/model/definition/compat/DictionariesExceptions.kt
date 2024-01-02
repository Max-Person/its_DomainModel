package its.model.definition.compat

import its.model.definition.DomainDefinitionException

/**
 * Ошибка при построении модели в [DomainDictionariesBuilder]
 *
 * Оборачивает [DomainDefinitionException] (См. [cause]), предоставляющая информацию о
 * соответствующей строке LOQI
 */
open class DictionariesDomainBuildException : DomainDefinitionException {
    val dictionary: String
    val lineIndex: Int

    constructor(cause: DomainDefinitionException)
            : super(cause.message?.withErrPrefix() ?: prefix, cause) {
        lineIndex = -1
        dictionary = ""
    }

    constructor(line: Int, inDict: String, cause: DomainDefinitionException)
            : super(cause.message?.withErrPrefix(line, inDict) ?: prefix, cause) {
        lineIndex = line
        dictionary = inDict
    }

    constructor(line: Int, inDict: String, message: String)
            : super(message.withErrPrefix(line, inDict)) {
        lineIndex = line
        dictionary = inDict
    }

    companion object {
        private const val prefix = "Error on Dictionary model build"
        private fun String.withErrPrefix() = "$prefix: $this"
        private fun String.withErrPrefix(line: Int, dictionary: String) =
            "(at line $line of $dictionary dictionary) $prefix: $this"
    }
}