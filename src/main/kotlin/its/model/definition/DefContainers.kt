package its.model.definition


/**
 * Хранилище определений в домене
 */
sealed class DefContainer<T : DomainDef<T>> : DomainElement(), Collection<T> {
    private val values: MutableMap<String, T> = mutableMapOf()
    override fun iterator() = values.values.iterator()

    private val builtInValues: MutableMap<String, T> = mutableMapOf()

    /**
     * Добавить встроенные значения: значения, которые всегда есть в контейнерах данного типа,
     * но не учитываются как пользовательские
     *
     * Данную функцию нужно вызывать **только** в конструкторах контейнеров,
     * чтобы данные значения существовали всегда и не менялись
     */
    protected fun addBuiltIn(def: T) = addNew(def, builtInValues)

    protected abstract fun KEY_REPEAT_MESSAGE(def: T): String

    /**
     * Добавить определение (при добавлении определение валидируется)
     * @throws DomainDefinitionException если такое определение уже есть, или если возникли ошибки валидации
     * @see addMerge
     */
    fun add(def: T) = add(def, false)

    /**
     * Добавить определение (при добавлении определение валидируется);
     * Также пытается слить одинаковые определения
     * (пример: два класса с одним именем становятся одним, со свойствами из обоих)
     * @throws DomainDefinitionException если возникли ошибки валидации при слитии или добавлении
     * @see DomainDef.addMerge
     */
    fun addMerge(def: T) = add(def, true)

    private fun add(def: T, tryMerging: Boolean): T {
        val existing = get(def.name)
        if (existing != null) {
            if (tryMerging && existing.mergeEquals(def)) {
                existing.addMerge(def)
                return existing
            } else {
                invalid(KEY_REPEAT_MESSAGE(def))
            }
        }

        return addNew(def)
    }

    protected open fun addNew(def: T): T = addNew(def, values)

    private fun addNew(def: T, addTo: MutableMap<String, T>): T {
        //Приведение к нужному хранимому виду
        val belongsToDomain = def.belongsToDomain
        val added = if (belongsToDomain == domain) {
            def
        } else {
            def.copyForDomain(domain)
        }
        added.validateAndThrowInvalid()

        //добавление
        if (added is DomainDefWithMeta<*> && domain.separateMetadata != null) { //Проверка на нулл нужна, т.к. при добавлении встроенных значений domain.separateMetadata еще может быть не инициализирован
            domain.separateMetadata.claimIfPresent(added)
        }
        addTo[added.name] = added
        return added
    }

    /**
     * @see add
     */
    fun addAll(other: DefContainer<T>) = other.forEach { add(it) }

    /**
     * @see addMerge
     */
    fun addAllMerge(other: DefContainer<T>) = other.forEach { addMerge(it) }

    fun remove(def: T) {
        if (get(def.name) == def) {
            remove(def.name)
        }
    }

    fun remove(name: String): T? {
        return values.remove(name)
    }

    fun subtract(other: DefContainer<T>) {
        for (def in other) {
            val existing = get(def.name) ?: continue
            if (!existing.mergeEquals(def)) continue

            existing.subtract(def)
            if (existing.isEmpty) {
                remove(existing)
            }
        }
    }

    /**
     * Получить определение по имени
     */
    fun get(name: String): T? {
        return values[name] ?: builtInValues[name]
    }

    override fun validate(results: DomainValidationResults) {
        this.forEach { it.validate(results) }
    }

    override val size: Int
        get() = values.size

    override fun contains(element: T): Boolean {
        return get(element.name) == element
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return elements.all { this.contains(it) }
    }

    override fun isEmpty(): Boolean {
        return values.isEmpty()
    }
}

sealed class RootDefContainer<T : DomainDef<T>>(override val domain: Domain) : DefContainer<T>() {
    override fun KEY_REPEAT_MESSAGE(def: T) = "Domain already contains definition for ${def.description}"
}

sealed class ChildDefContainer<T : DomainDef<T>, Owner : DomainDef<Owner>>(private val owner: Owner) :
    DefContainer<T>() {
    override val domain: Domain
        get() = owner.domain

    override fun KEY_REPEAT_MESSAGE(def: T) = "${owner.description} already contains definition for ${def.description}"
}