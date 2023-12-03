package its.model.definition

/**
 * Утверждение об элементе домена
 */
abstract class Statement<Owner : DomainDef> : DomainElement() {
    abstract val owner: Owner
    override val domain: Domain
        get() = owner.domain

    internal abstract fun copyForOwner(owner: Owner): Statement<Owner>

    override fun toString() = description
}

/**
 * Набор однотипных утверждений, принадлежащих одному элементу домена
 */
abstract class Statements<Owner : DomainDef, S : Statement<Owner>>(
    protected val owner: Owner,
) : DomainElement(), Collection<S> {
    override fun toString() = description

    override val domain: Domain
        get() = owner.domain

    fun add(statement: S): S {
        val copy = if (statement.owner != owner) statement.copyForOwner(owner) as S else statement
        copy.validateAndThrowInvalid()
        addToInner(copy)
        return copy
    }

    protected abstract fun addToInner(statement: S)

    override fun validate(results: DomainValidationResults) {
        this.forEach { it.validate(results) }
    }
}