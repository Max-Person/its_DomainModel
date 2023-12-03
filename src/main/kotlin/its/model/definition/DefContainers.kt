package its.model.definition

import java.util.*

/**
 * Хранилище определений в домене
 */
sealed class DefContainer<T : DomainDef> : DomainElement(), Collection<T> {
    private val values: MutableMap<String, T> = mutableMapOf()
    override fun iterator() = values.values.iterator()

    protected abstract fun KEY_REPEAT_MESSAGE(def: T): String

    /**
     * Добавить определение
     */
    open fun add(def: T): T {
        val belongsToDomain = def.belongsToDomain
        val added = if (belongsToDomain.isPresent && belongsToDomain.get() != domain) {
            def.copyForDomain(domain) as T
        } else {
            def.domain = domain
            def
        }

        if (added is DomainDefWithMeta) {
            domain.separateMetadata.claimIfPresent(added)
        }
        added.validateAndThrowInvalid()
        checkValid(
            !values.containsKey(def.name),
            KEY_REPEAT_MESSAGE(def)
        )
        values[added.name] = added
        return added
    }

    /**
     * Получить определение по имени
     */
    fun get(name: String): Optional<T> {
        return Optional.ofNullable(values[name])
    }

    /**
     * TODO Скопировать информацию в другой контейнер
     */
    fun copyTo(other: DefContainer<T>) {

    }

    override fun validate(results: DomainValidationResults) {
        this.forEach { it.validate(results) }
    }

    override val size: Int
        get() = values.size

    override fun contains(element: T): Boolean {
        return values.containsKey(element.name) && values[element.name] == element
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { this.contains(it) }
    }

    override fun isEmpty(): Boolean {
        return values.isEmpty()
    }
}

sealed class RootDefContainer<T : DomainDef>(override val domain: Domain) : DefContainer<T>() {
    override fun KEY_REPEAT_MESSAGE(def: T) = "Domain already contains definition for ${def.description}"
}

sealed class ChildDefContainer<T : DomainDef, Owner : DomainDef>(private val owner: Owner) : DefContainer<T>() {
    override val domain: Domain
        get() = owner.domain

    override fun KEY_REPEAT_MESSAGE(def: T) = "${owner.description} already contains definition for ${def.description}"
}