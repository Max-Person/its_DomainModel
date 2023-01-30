package its.model.models

/**
 * Модель отношения в предметной области
 * @param name Имя отношения
 * @param parent Имя родительского отношения
 * @param argsClasses Имена классов аргументов
 * @param scaleType Тип порядковой шкалы
 * @param scaleRelationshipsNames Имена отношений, задаваемых этим базовым отношением
 * @param relationType Тип связи между классами
 * @param flags Флаги свойств отношения
 */
open class RelationshipModel(
    val name: String,
    val parent: String? = null,
    val argsClasses: List<String>,
    val scaleType: ScaleType? = null,
    val scaleRelationshipsNames: List<String>? = null,
    val relationType: RelationType? = null,
    flags: Int,
) {
    val flags : Int
    init {
        if(flags == 0 && scaleType == ScaleType.Linear)
            this.flags = 6
        else if(flags == 0 && scaleType == ScaleType.Partial)
            this.flags = 22
        else
            this.flags = flags
    }

    /**
     * Создает набор производных отношений порядковой шкалы
     *
     * *Важно:* Все элементы возвращаемого массива должны быть объектами реализующего класса (наследника)
     */
    open fun scaleRelationships():List<RelationshipModel>{
        when (scaleType) {
            ScaleType.Linear -> {
                scaleRelationshipsNames!!
                return listOf(
                    RelationshipModel(
                        name = scaleRelationshipsNames[0],
                        argsClasses = argsClasses,
                        flags = flags
                    ),
                    RelationshipModel(
                        name = scaleRelationshipsNames[1],
                        argsClasses = argsClasses,
                        flags = 16
                    ),
                    RelationshipModel(
                        name = scaleRelationshipsNames[2],
                        argsClasses = argsClasses,
                        flags = 16
                    ),
                    RelationshipModel(
                        name = scaleRelationshipsNames[3],
                        argsClasses = argsClasses.plus(argsClasses[0]),
                        flags = 0
                    ),
                    RelationshipModel(
                        name = scaleRelationshipsNames[4],
                        argsClasses = argsClasses.plus(argsClasses[0]),
                        flags = 0
                    ),
                    RelationshipModel(
                        name = scaleRelationshipsNames[5],
                        argsClasses = argsClasses.plus(argsClasses[0]),
                        flags = 0
                    )
                )
            }

            ScaleType.Partial -> {
                TODO("Отношения частичного порядка")
                return emptyList()
            }

            else -> {
                return emptyList()
            }
        }
    }

    /**
     * Проверяет корректность модели
     * @throws IllegalArgumentException
     */
    open fun validate() {
        require(name.isNotBlank()) {
            "Некорректное имя отношения."
        }
        require(argsClasses.size >= 2) {
            "Отношение $name имеет меньше двух аргументов."
        }
        require(scaleType == null || argsClasses.size == 2 && argsClasses[0] == argsClasses[1]) {
            "Отношение порядковой шкалы может быть только бинарным и только между объектами одного класса."
        }
        when (scaleType) {
            ScaleType.Linear -> require(flags == 6) {
                "Некорректный набор флагов для отношения линейного порядка."
            }

            ScaleType.Partial -> require(flags == 22) {
                "Некорректный набор флагов для отношения частичного порядка."
            }

            else -> require(flags < 64) {
                "Некорректный набор флагов."
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RelationshipModel

        if (name != other.name) return false
        if (parent != other.parent) return false
        if (argsClasses != other.argsClasses) return false
        if (scaleType != other.scaleType) return false
        if (scaleRelationshipsNames != other.scaleRelationshipsNames) return false
        if (relationType != other.relationType) return false
        if (flags != other.flags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        result = 31 * result + argsClasses.hashCode()
        result = 31 * result + (scaleType?.hashCode() ?: 0)
        result = 31 * result + (scaleRelationshipsNames?.hashCode() ?: 0)
        result = 31 * result + (relationType?.hashCode() ?: 0)
        result = 31 * result + flags
        return result
    }

    /**
     * Является ли отношение симметричным
     */
    val isSymmetric
        get() = flags and SYMMETRIC != 0

    /**
     * Является ли отношение анти-симметричным
     */
    val isAntisymmetric
        get() = flags and ANTISYMMETRIC != 0

    /**
     * Является ли отношение рефлексивным
     */
    val isReflexive
        get() = flags and REFLEXIVE != 0

    /**
     * Является ли отношение анти-рефлексивным (строгим)
     */
    val isStrict
        get() = flags and STRICT != 0

    /**
     * Является ли отношение транзитивным
     */
    val isTransitive
        get() = flags and TRANSITIVE != 0

    /**
     * Является ли отношение анти-транзитивным
     */
    val isIntransitive
        get() = flags and INTRANSITIVE != 0

    /**
     * Типы связей между классами
     */
    enum class RelationType {

        /**
         * Один к одному
         */
        OneToOne,

        /**
         * Один ко многим
         */
        OneToMany;

        companion object _static {
            @JvmStatic
            fun fromString(value: String) = when (value.uppercase()) {
                "ONETOONE","ONE_TO_ONE" -> OneToOne
                "ONETOMANY","ONE_TO_MANY" -> OneToMany
                else -> null
            }
        }
    }

    /**
     * Тип порядковой шкалы
     */
    enum class ScaleType {

        /**
         * Линейный порядок
         */
        Linear,

        /**
         * Частичный порядок
         */
        Partial;

        companion object _static {
            @JvmStatic
            fun fromString(value: String) = when (value.uppercase()) {
                "LINEAR" -> Linear
                "PARTIAL" -> Partial
                else -> null
            }
        }
    }

    companion object _static {

        /**
         * Флаг симметричности отношения
         */
        const val SYMMETRIC = 1

        /**
         * Флаг анти-симметричности отношения
         */
        const val ANTISYMMETRIC = 2

        /**
         * Флаг рефлексивности отношения
         */
        const val REFLEXIVE = 4

        /**
         * Флаг анти-рефлексивности (строгости) отношения
         */
        const val STRICT = 8

        /**
         * Флаг транзитивности отношения
         */
        const val TRANSITIVE = 16

        /**
         * Флаг анти-транзитивности отношения
         */
        const val INTRANSITIVE = 32
    }


}