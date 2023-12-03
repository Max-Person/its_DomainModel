package its.model.definition.loqi

import its.model.definition.DomainDefinitionException

/**
 * Ошибка при построении модели в [LoqiDomainBuilder]
 *
 * Оборачивает [DomainDefinitionException] (См. [cause]), предоставляющая информацию о
 * соответствующей строке LOQI
 */
open class LoqiDomainBuildException : DomainDefinitionException {
    var lineIndex = -1

    constructor() : super()
    constructor(message: String) : super(message.withErrPrefix())
    constructor(message: String, cause: Throwable) : super(message.withErrPrefix(), cause)

    constructor(line: Int, message: String) : super(message.withErrPrefix(line)) {
        lineIndex = line
    }

    constructor(line: Int, message: String, cause: Throwable) : super(message.withErrPrefix(line), cause) {
        lineIndex = line
    }

    companion object {
        private fun String.withErrPrefix() = "Error on LOQI model build: $this"
        private fun String.withErrPrefix(line: Int) = "(at line $line) Error on LOQI model build: $this"
    }
}