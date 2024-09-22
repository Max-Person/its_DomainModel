package its.model.definition

/**
 * Именованное определение внутри домена
 */
sealed class DomainDef<Self : DomainDef<Self>> : DomainElement(), Cloneable {
    /**
     * Имя данного определения
     */
    abstract val name: String
    override fun toString() = description
    abstract val reference: DomainRef<Self>

    internal var domainModelInternal: DomainModel? = null
        private set

    override var domainModel: DomainModel
        internal set(value) {
            preventMisuse(
                this.domainModelInternal == null || this.domainModelInternal == value,
                "Attempting to change the 'domain' property of a $description once it's set"
            )
            this.domainModelInternal = value
        }
        get() : DomainModel {
            preventMisuse(
                this.domainModelInternal != null,
                "Attempting to access the 'domain' property of a $description before it is set"
            )
            return this.domainModelInternal!!
        }

    /**
     * Создать глубокую копию данного определения;
     * Копия не принадлежит ни к какому домену и полностью повторяет состояние данного определения
     */
    fun deepCopy() = plainCopy().also { it.addMerge(this as Self) }

    internal fun copyForDomain(domainModel: DomainModel) =
        plainCopy().also {
            it.domainModel = domainModel
            it.addMerge(this as Self)
        }

    /**
     * Создать базовую копию данного определения;
     * Копия не принадлежит ни к какому домену, и не содержит никакого состояния данного определения,
     * кроме основных зарактеристик (параметров основного конструктора)
     * @return копия - определение того же типа, что и данное, такое что `copy.mergeEquals(this) == true`
     */
    abstract fun plainCopy(): Self

    /**
     * Являются ли определения одинаковыми по основным характеристикам - возможно ли слияние между ними
     * @see addMerge
     */
    open fun mergeEquals(other: Self): Boolean {
        return this.reference == other.reference
    }

    /**
     * Добавить информацию из [other] в данное определение.
     * Должно использоваться, только если `this.mergeEquals(other) == true`
     * @see mergeEquals
     */
    open fun addMerge(other: Self) {
        preventMisuse(
            this.mergeEquals(other),
            "'addMerge()' should only be called if both defs are 'merge equal'. Use 'mergeEquals' to check"
        )
    }

    open fun subtract(other: Self) {
        preventMisuse(
            this.mergeEquals(other),
            "'subtract()' should only be called if both defs are 'merge equal'. Use 'mergeEquals' to check"
        )
    }

    open val isEmpty: Boolean
        get() = true

    override fun validate(results: DomainValidationResults) {
        results.checkValid(
            name.isValidName(),
            "'$name' is not a valid name for a domain definition"
        )
    }

    /**
     * Является ли валидным именем - не пустое и не содержит пробелов и бэктиков
     */
    private fun String.isValidName(): Boolean {
        return isNotBlank() && !this.contains("[\\s`]".toRegex())
    }
}

/**
 * Ссылка на определение в домене - минимальная необходимая информация, чтобы найти данный элемент
 */
sealed interface DomainRef<Def : DomainDef<Def>> {
    /**
     * Найти в домене [domainModel] определение, на которое ссылается эта ссылка, если оно есть
     */
    fun findIn(domainModel: DomainModel): Def?

    fun findInOrUnkown(domainModel: DomainModel): Def {
        val found = findIn(domainModel)
        checkKnown(
            found != null,
            "No definition for reference '$this' found in domain"
        )
        return found!!
    }
}