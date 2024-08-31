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
) {
    private val propertyNamesToLocalizations = mutableMapOf<String, MutableMap<String?, Any>>()

    /**
     * Получить список всех локализаций переданного свойства
     * Возвращает мапу {код локализации -> значение свойства}
     */
    fun getLocalizations(propertyName: String): Map<String?, Any> {
        return propertyNamesToLocalizations[propertyName] ?: emptyMap()
    }

    /**
     * Получить список всех локализаций переданного свойства, с предположением что они все строковые
     * Возвращает мапу {код локализации -> значение свойства}
     */
    fun getStringLocalizations(propertyName: String): Map<String?, String> {
        return getLocalizations(propertyName)
            .map { (locCode, value) -> locCode to value as String }
            .toMap()
    }

    private operator fun get(key: MetadataProperty): Any? {
        return if (getLocalizations(key.name).containsKey(key.locCode)) {
            getLocalizations(key.name)[key.locCode]
        } else if (owner is ClassInheritorDef<*> && owner.parentClass != null) {
            owner.parentClass!!.metadata[key]
        } else {
            null
        }
    }

    /**
     * Получить нелокализованные метаданные
     */
    operator fun get(name: String) = get(MetadataProperty(null, name))

    /**
     * Получить локализированные метаданные
     */
    operator fun get(locCode: String?, name: String) = get(MetadataProperty(locCode, name))

    /**
     * Получить нелокализованные строковые метаданные
     */
    fun getString(name: String) = get(name)!! as String

    /**
     * Получить локализированные строковые метаданные
     */
    fun getString(locCode: String?, name: String) = get(locCode, name)!! as String

    private fun add(property: MetadataProperty, value: Any) {
        propertyNamesToLocalizations.merge(
            property.name,
            mutableMapOf(property.locCode to value)
        ) { thisLocCodes, otherLocCodes ->
            thisLocCodes.apply { putAll(otherLocCodes) }
        }
    }

    /**
     * Добавить нелокализованные метаданные
     */
    fun add(name: String, value: Any) = add(MetadataProperty(null, name), value)

    /**
     * Добавить локализированные метаданные
     */
    fun add(locCode: String?, name: String, value: Any) = add(MetadataProperty(locCode, name), value)

    /**
     * Добавить (без очистки, с перезаписью) метаданные из [other]
     */
    fun addAll(other: MetaData) {
        other.propertyNamesToLocalizations.forEach { name, locCodes ->
            this.propertyNamesToLocalizations.merge(name, locCodes) { thisLocCodes, otherLocCodes ->
                thisLocCodes.apply { putAll(otherLocCodes) }
            }
        }
    }

    private fun remove(property: MetadataProperty) {
        propertyNamesToLocalizations[property.name]?.apply {
            remove(property.locCode)
            if (isEmpty()) {
                propertyNamesToLocalizations.remove(property.name)
            }
        }
    }

    fun subtract(other: MetaData) {
        for (propertyValue in other.entries) {
            val key = propertyValue.justProperty()
            val existing = get(key) ?: continue
            if (existing == propertyValue.value) {
                remove(key)
            }
        }
    }

    /**
     * Есть ли в метаданных свойство [propertyName]
     */
    fun containsAny(propertyName: String): Boolean {
        return propertyNamesToLocalizations.containsKey(propertyName)
    }

    /**
     * Есть ли в метаданных нелокализованныое свойство [propertyName]
     */
    fun containsUnlocalized(propertyName: String): Boolean {
        return get(propertyName) != null
    }

    /**
     * Есть ли в метаданных локализованныое свойство [locCode].[propertyName]
     */
    fun containsLocalized(locCode: String, propertyName: String): Boolean {
        return get(locCode, propertyName) != null
    }

    val size: Int
        get() = propertyNamesToLocalizations.size

    fun isEmpty() = propertyNamesToLocalizations.isEmpty()
    fun isNotEmpty() = !isEmpty()

    val entries: Set<MetadataPropertyValue>
        get() = propertyNamesToLocalizations.flatMap { nameToLocCodes ->
            nameToLocCodes.value.map { locCodeToValue ->
                MetadataPropertyValue(locCodeToValue.key, nameToLocCodes.key, locCodeToValue.value)
            }
        }.toSet()


    companion object {
        private class SyntheticOwner : MetaOwner {
            override val metadata = MetaData(this)
            override val description = "SEPARATE_META_SYNTHETIC_OWNER"
        }

        @JvmStatic
        fun separate() = SyntheticOwner().metadata
    }
}

/**
 * Вспомогательный класс - свойство в метаданных
 * @param locCode Код локализации свойства (для локализируемых)
 * @param name Имя свойства
 */
data class MetadataProperty(
    val locCode: String?,
    val name: String,
)

/**
 * Значение свойства в метаданных
 * @param locCode Код локализации свойства (для локализируемых)
 * @param propertyName Имя свойства
 * @param value Значение свойства
 */
data class MetadataPropertyValue(
    val locCode: String?,
    val propertyName: String,
    val value: Any,
) {
    fun justProperty() = MetadataProperty(locCode, propertyName)
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

    override val isEmpty: Boolean
        get() = super.isEmpty && metadata.isEmpty()

    override fun subtract(other: Self) {
        super.subtract(other)
        metadata.subtract(other.metadata)
    }
}