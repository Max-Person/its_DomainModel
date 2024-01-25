package its.model.definition

/**
 * Ошибка при работе с доменом
 */
open class DomainUseException : IllegalArgumentException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Несоответствии выполняемой на домене операции определениям внутри домена
 *
 * Данные исключения выкидываются, когда домен был полностью провалидирован на валидность и полноту,
 * и сообщают о некорректности выполняемых операция с учетом того, что уже определено в домене
 */
open class DomainNonConformityException : DomainUseException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Отсутствие ожидаемых метаданных
 */
class NoMetadataException : DomainNonConformityException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

internal fun checkConforming(condition: Boolean, message: String) {
    if (!condition) {
        nonConforming(message)
    }
}

internal fun nonConforming(message: String): Nothing {
    throw DomainNonConformityException(message)
}