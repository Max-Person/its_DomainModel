package its.model.definition

/**
 * Контейнер для отдельно хранимых данных (конструкции 'values for ...' и 'meta for ...' в LOQI)
 */
abstract class SeparateDataContainer<Key : DomainRef, Value : Any>(
    override val domain: Domain
) : DomainElement(), Map<Key, Value> {
    protected val map: MutableMap<Key, Value> = mutableMapOf()

    /**
     * Добавить данные (или сразу же прикрепить их к владельцу, если он есть)
     */
    fun add(key: Key, value: Value) {
        val owner = key.findIn(domain)
        owner.ifPresentOrElse({ attachValue(it, value) }, { map[key] = value })
    }

    /**
     * Забрать из контейнера и прикрепить данные, принадлежащие владельцу [domainDef], если такие есть
     */
    fun claimIfPresent(domainDef: DomainDefWithMeta) {
        val ref = domainDef.reference
        if (map.containsKey(ref)) {
            val value = map.remove(ref)!!
            attachValue(domainDef, value)
        }
    }

    /**
     * "Прикрепить" данные [value] к владельцу [domainDef]
     */
    abstract fun attachValue(domainDef: DomainDefWithMeta, value: Value)

    override val entries: Set<Map.Entry<Key, Value>>
        get() = map.entries
    override val keys: Set<Key>
        get() = map.keys
    override val size: Int
        get() = map.size
    override val values: Collection<Value>
        get() = map.values

    override fun containsKey(key: Key) = map.containsKey(key)
    override fun containsValue(value: Value) = map.containsValue(value)
    override fun get(key: Key) = map[key]
    override fun isEmpty() = map.isEmpty()
}

class SeparateClassPropertyValuesContainer(
    domain: Domain
) : SeparateDataContainer<ClassRef, List<ClassPropertyValueStatement>>(domain) {

    override fun attachValue(domainDef: DomainDefWithMeta, value: List<ClassPropertyValueStatement>) {
        if (domainDef !is ClassDef) return
        value.forEach { statement -> domainDef.definedPropertyValues.add(statement) }
    }

    override fun validate(results: DomainValidationResults) {
        for (classRef in map.keys) {
            results.unknown("No class '${classRef.className}' found to attach separate property values")
        }
    }
}

class SeparateMetadataContainer(
    domain: Domain
) : SeparateDataContainer<DomainRef, MetaData>(domain) {
    override fun attachValue(domainDef: DomainDefWithMeta, value: MetaData) {
        checkValid(
            domainDef.metadata.isEmpty(),
            "Attempting to replace metadata for ${domainDef.description}"
        )
        domainDef.metadata.addAll(value)
    }

    override fun validate(results: DomainValidationResults) {
        for (classRef in map.keys) {
            results.unknown("No definition matching reference '$classRef' found to attach separate metadata")
        }
    }
}