package its.model

import its.model.definition.DomainModel
import its.model.definition.DomainNonConformityException
import its.model.definition.DomainUseException
import java.util.*

/**
 * Ошибка построения зависимой от домена [DomainModel] структуры
 */
open class InvalidDomainConstructException : DomainUseException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other::class != this::class) return false
        if ((other as InvalidDomainConstructException).message != this.message) return false
        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(javaClass, message)
    }
}

/**
 * Хранилище ошибок валидации зависимой от домена [DomainModel] структуры
 */
open class DomainConstructValidationResults(val throwImmediately: Boolean = false) {
    val invalids = mutableSetOf<InvalidDomainConstructException>()
    val nonConforming = mutableSetOf<DomainNonConformityException>()

    internal open fun addAll(other: DomainConstructValidationResults) {
        if (throwImmediately) other.throwAll()
        invalids.addAll(other.invalids)
        nonConforming.addAll(other.nonConforming)
    }

    internal open fun add(e: IllegalArgumentException) {
        if (throwImmediately) throw e
        when (e) {
            is InvalidDomainConstructException -> invalids.add(e)
            is DomainNonConformityException -> nonConforming.add(e)
        }
    }

    internal fun checkValid(condition: Boolean, message: String) {
        if (!condition) {
            invalid(message)
        }
    }

    protected open fun createInvalid(message: String) = InvalidDomainConstructException(message)

    internal fun invalid(message: String) {
        val e = createInvalid(message)
        e.fillInStackTrace()
        add(e)
    }

    internal fun checkConforming(condition: Boolean, message: String) {
        if (!condition) {
            nonConforming(message)
        }
    }

    internal fun nonConforming(message: String) {
        val e = DomainNonConformityException(message)
        e.fillInStackTrace()
        add(e)
    }

    fun throwAll() {
        invalids.firstOrNull()?.apply { throw this }
        nonConforming.firstOrNull()?.apply { throw this }
    }
}