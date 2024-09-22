package its.model.definition


/**
 * Хранилище определений в домене
 */
sealed class DefContainer<T : DomainDef<T>> : DomainElement(), MutableCollection<T> {
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
     * @see added
     * @see addMerge
     */
    override fun add(element: T): Boolean {
        added(element)
        return true
    }

    /**
     * Добавить определение (при добавлении определение валидируется)
     * Метод аналогичен [add], но возвращает добавленный элемент, поэтому удобен при наполнении моделей.
     * @throws DomainDefinitionException если такое определение уже есть, или если возникли ошибки валидации
     * @see addMerge
     */
    fun added(def: T) = add(def, false)

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
        val ownerDomainModel = def.domainModelInternal
        val added = if (ownerDomainModel == domainModel) {
            def
        } else {
            def.copyForDomain(domainModel)
        }
        added.validateAndThrowInvalid()

        //добавление
        if (added is DomainDefWithMeta<*> && domainModel.separateMetadata != null) { //Проверка на нулл нужна, т.к. при добавлении встроенных значений domain.separateMetadata еще может быть не инициализирован
            domainModel.separateMetadata.claimIfPresent(added)
        }
        addTo[added.name] = added
        return added
    }

    /**
     * @see add
     */
    override fun addAll(elements: Collection<T>): Boolean {
        elements.forEach { add(it) }
        return true
    }

    /**
     * @see addMerge
     */
    fun addAllMerge(other: Collection<T>) = other.forEach { addMerge(it) }

    override fun remove(element: T): Boolean {
        if (get(element.name) == element) {
            return remove(element.name) != null
        }
        return false;
    }

    fun remove(name: String): T? {
        return values.remove(name)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var removed = false
        for (el in elements) {
            removed = remove(el) || removed
        }
        return removed
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val toRemove = this.filter { !elements.contains(it) }
        return removeAll(toRemove)
    }

    override fun clear() {
        values.clear()
    }

    fun subtract(other: Collection<T>) {
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

sealed class RootDefContainer<T : DomainDef<T>>(override val domainModel: DomainModel) : DefContainer<T>() {
    override fun KEY_REPEAT_MESSAGE(def: T) = "Domain already contains definition for ${def.description}"
}

sealed class ChildDefContainer<T : DomainDef<T>, Owner : DomainDef<Owner>>(private val owner: Owner) :
    DefContainer<T>() {
    override val domainModel: DomainModel
        get() = owner.domainModel

    override fun KEY_REPEAT_MESSAGE(def: T) = "${owner.description} already contains definition for ${def.description}"
}