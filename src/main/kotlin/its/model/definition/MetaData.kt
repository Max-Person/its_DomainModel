package its.model.definition

/**
 * Метаданные элементов домена
 *
 * Под метаданными понимаются любые дополнительные данные в домене,
 * не попадающие под внутренние смысловые ограничения (т.е. не валидируемые).
 * Подобные метаданные могут привязываться к большинству определений в домене
 *
 * TODO добавить возможность кастомной валидации метаданных
 */
class MetaData(
    private val owner: MetaOwner,
) : Map<MetadataProperty, Any> {
    private val declaredValues = mutableMapOf<MetadataProperty, Any>()

    /**
     * Получить метаданные
     */
    override fun get(key: MetadataProperty): Any? {
        return if (declaredValues.containsKey(key)) {
            declaredValues[key]!!
        } else if (owner is ClassInheritorDef<*> && owner.parentClass != null) {
            owner.parentClass!!.metadata[key]
        } else {
            null
        }
    }

    operator fun get(key: String) = get(MetadataProperty(key))

    /**
     * Получить метаданные, при уверенности что они есть
     * @throws [NoMetadataException] если метаданных не оказалось
     */
    fun getAsserted(property: MetadataProperty): Any {
        return get(property)
            ?: throw NoMetadataException("No metadata property ${property.name} found for ${owner.description}")
    }

    fun getAsserted(property: String) = getAsserted(MetadataProperty(property))

    /**
     * Добавить метаданные
     */
    fun add(property: MetadataProperty, value: Any) {
        declaredValues[property] = value
    }

    /**
     * Добавить (без очистки, с перезаписью) метаданные из [other]
     */
    fun addAll(other: MetaData) {
        declaredValues.putAll(other.declaredValues)
    }

    override fun isEmpty() = declaredValues.isEmpty()
    override val entries: Set<Map.Entry<MetadataProperty, Any>>
        get() = declaredValues.entries
    override val keys: Set<MetadataProperty>
        get() = declaredValues.keys
    override val size: Int
        get() = declaredValues.size
    override val values: Collection<Any>
        get() = declaredValues.values

    override fun containsKey(key: MetadataProperty) = declaredValues.containsKey(key)
    override fun containsValue(value: Any) = declaredValues.containsValue(value)
}

/**
 * Свойство в метаданных
 * @param name Имя свойства
 * @param locCode Код локализации свойства (для локализируемых)
 */
data class MetadataProperty(
    val name: String,
    val locCode: String? = null,
) {

    constructor(string: String) : this(
        if (string.split(LOC_CODE_DELIMITER, limit = 2).size < 2) string
        else string.split(LOC_CODE_DELIMITER, limit = 2)[1],

        if (string.split(LOC_CODE_DELIMITER, limit = 2).size < 2) null
        else string.split(LOC_CODE_DELIMITER, limit = 2)[0],
    )

    companion object {
        const val LOC_CODE_DELIMITER = "."
    }
}

/**
 * Владелец метаданных
 */
interface MetaOwner {
    /**
     * Метаданные данного элемента
     */
    val metadata: MetaData
    val description: String
}

sealed class DomainDefWithMeta<Self : DomainDefWithMeta<Self>> : DomainDef<Self>(), MetaOwner {
    override val metadata = MetaData(this)
    override fun addMerge(other: Self) {
        super.addMerge(other)
        this.metadata.addAll(other.metadata)
    }
}