package its.model.definition

/**
 * Утверждение об элементе домена
 */
abstract class Statement<Owner : DomainDef<Owner>> : DomainElement() {
    abstract val owner: Owner
    override val domainModel: DomainModel
        get() = owner.domainModel

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
) : DomainElement(), MutableCollection<S> {
    override fun toString() = description

    override val domainModel: DomainModel
        get() = owner.domainModel

    protected fun copyForOwner(statement: S): S =
        if (statement.owner != owner) statement.copyForOwner(owner) as S else statement

    fun addAndGet(statement: S): S {
        val copy = copyForOwner(statement)
        copy.validateAndThrowInvalid()
        addToInner(copy)
        return copy
    }

    override fun add(element: S): Boolean {
        addAndGet(element)
        return true
    }

    override fun addAll(elements: Collection<S>): Boolean {
        elements.forEach { add(it) }
        return true
    }

    protected abstract fun addToInner(statement: S)

    override fun removeAll(elements: Collection<S>): Boolean {
        var removed = false
        for (el in elements) {
            removed = remove(el) || removed
        }
        return removed
    }

    override fun retainAll(elements: Collection<S>): Boolean {
        val toRemove = this.filter { !elements.contains(it) }
        return removeAll(toRemove)
    }

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