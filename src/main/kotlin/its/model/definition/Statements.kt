package its.model.definition

/**
 * Утверждение об элементе домена
 */
abstract class Statement<Owner : DomainDef<Owner>> : DomainElement() {
    abstract val owner: Owner
    override val domain: Domain
        get() = owner.domain

    internal abstract fun copyForOwner(owner: Owner): Statement<Owner>

    override fun toString() = description

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
}

/**
 * Набор однотипных утверждений, принадлежащих одному элементу домена
 */
abstract class Statements<Owner : DomainDef<Owner>, S : Statement<Owner>>(
    protected val owner: Owner,
) : DomainElement(), Collection<S> {
    override fun toString() = description

    override val domain: Domain
        get() = owner.domain

    protected fun copyForOwner(statement: S): S =
        if (statement.owner != owner) statement.copyForOwner(owner) as S else statement

    fun add(statement: S): S {
        val copy = copyForOwner(statement)
        copy.validateAndThrowInvalid()
        addToInner(copy)
        return copy
    }

    fun addAll(statements: Collection<S>) = statements.forEach { add(it) }

    protected abstract fun addToInner(statement: S)

    protected abstract fun remove(statement: S)

    fun subtract(other: Collection<S>) {
        for (statement in other) {
            val copy = copyForOwner(statement)
            remove(copy)
        }
    }

    override fun validate(results: DomainValidationResults) {
        this.forEach { it.validate(results) }
    }

    abstract override fun contains(element: S): Boolean
}