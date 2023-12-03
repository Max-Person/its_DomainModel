package its.model.definition

import java.util.*

/**
 * Именованное определение внутри домена
 */
sealed class DomainDef : DomainElement(), Cloneable {
    /**
     * Имя данного определения
     */
    abstract val name: String
    override fun toString() = description

    private var _domain: Domain? = null
    internal val belongsToDomain: Optional<Domain>
        get() = Optional.ofNullable(_domain)
    override var domain: Domain
        internal set(value) {
            preventMisuse(
                _domain == null || _domain == value,
                "Attempting to change the 'domain' property of a $description once it's set"
            )
            _domain = value
        }
        get() : Domain {
            preventMisuse(
                _domain != null,
                "Attempting to access the 'domain' property of a $description before it is set"
            )
            return _domain!!
        }

    /**
     * Скопировать это определение для сохранения в другом домене [domain]
     * TODO переопределить это во всех наследниках
     */
    internal open fun copyForDomain(domain: Domain): DomainDef {
        val copy = super.clone() as DomainDef
        copy._domain = domain
        return copy
    }

    override fun validate(results: DomainValidationResults) {
        results.checkValid(
            name.isValidName(),
            "'$name' is not a valid name for a domain definition"
        )
    }

    private fun String.isValidName(): Boolean {
        return isNotBlank() //TODO
    }
}

/**
 * Ссылка на определение в домене - минимальная необходимая информация, чтобы найти данный элемент
 * TODO? Добавить дженерик-типизацию для того, на что оно ссылается
 */
sealed interface DomainRef {
    /**
     * Найти в домене [domain] определение, на которое ссылается эта ссылка, если оно есть
     */
    fun findIn(domain: Domain): Optional<DomainDefWithMeta>
}