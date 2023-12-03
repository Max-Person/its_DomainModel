package its.model.definition

import java.util.*

/**
 * Ошибка при построении (определении) домена
 */
abstract class DomainDefinitionException : IllegalArgumentException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other::class != this::class) return false
        if ((other as DomainDefinitionException).message != this.message) return false
        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(javaClass, message)
    }
}

/**
 * Неизвестные данные при построении домена
 *
 * Это исключение выкидывается, если один из элементов домена ссылается на другой,
 * который на данный момент не объявлен в домене.
 *
 * В отличие от [InvalidDomainDefinitionException], эти ошибки могут быть исправлены добавлением данных.
 */
open class UnknownDomainDefinitionException : DomainDefinitionException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Некорректные данные при построении домена
 *
 * Это исключение выкидывается, если определения внутри домена не соответствуют внутренним смысловым ограничениям.
 *
 * В отличие от [UnknownDomainDefinitionException], эти ошибки не могут быть исправлены добавлением данных -
 * только их изменением.
 */
open class InvalidDomainDefinitionException : DomainDefinitionException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Результаты валидации модели.
 * Накапливает наборы [InvalidDomainDefinitionException] и [UnknownDomainDefinitionException],
 * возникающие при валидации
 */
open class DomainValidationResults {
    val invalids = mutableSetOf<InvalidDomainDefinitionException>()
    val unknowns = mutableSetOf<UnknownDomainDefinitionException>()

    internal open fun addAll(other: DomainValidationResults) {
        invalids.addAll(other.invalids)
        unknowns.addAll(other.unknowns)
    }

    internal open fun add(e: DomainDefinitionException) {
        when (e) {
            is InvalidDomainDefinitionException -> invalids.add(e)
            is UnknownDomainDefinitionException -> unknowns.add(e)
        }
    }

    internal fun checkValid(condition: Boolean, message: String) {
        if (!condition) {
            invalid(message)
        }
    }

    internal fun invalid(message: String) {
        val e = InvalidDomainDefinitionException(message)
        e.fillInStackTrace()
        add(e)
    }

    internal fun checkKnown(condition: Boolean, message: String) {
        if (!condition) {
            unknown(message)
        }
    }

    internal fun unknown(message: String) {
        val e = UnknownDomainDefinitionException(message)
        e.fillInStackTrace()
        add(e)
    }

    fun throwInvalid() {
        invalids.firstOrNull()?.apply { throw this }
    }

    fun throwAll() {
        throwInvalid()
        unknowns.firstOrNull()?.apply { throw this }
    }
}

class DomainValidationResultsThrowImmediately : DomainValidationResults() {
    override fun add(e: DomainDefinitionException) {
        throw e
    }

    override fun addAll(other: DomainValidationResults) {
        other.throwAll()
    }
}


internal fun checkValid(condition: Boolean, message: String) {
    if (!condition) {
        invalid(message)
    }
}

internal fun invalid(message: String) {
    throw InvalidDomainDefinitionException(message).fillInStackTrace()
}

internal fun checkKnown(condition: Boolean, message: String) {
    if (!condition) {
        unknown(message)
    }
}

internal fun unknown(message: String) {
    throw UnknownDomainDefinitionException(message).fillInStackTrace()
}



