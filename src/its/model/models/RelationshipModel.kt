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
data class RelationshipModel(
    val name: String,
    val parent: String? = null,
    val argsClasses: List<String>,
    val scaleType: ScaleType? = null,
    val scaleRelationshipsNames: List<String>? = null,
    val relationType: RelationType? = null,
    val flags: Int,
) {

    fun scaleRelationships():List<RelationshipModel>{
        scaleRelationshipsNames!!
        when (scaleType) {
            ScaleType.Linear -> {
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
    fun validate() {
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

    companion object {

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

        /**
         * Типы связей между классами
         */
        sealed class RelationType {

            /**
             * Один к одному
             */
            object OneToOne : RelationType()

            /**
             * Один ко многим
             */
            object OneToMany : RelationType()

            companion object {

                fun valueOf(value: String) = when (value) {
                    "ONE_TO_ONE" -> OneToOne
                    "ONE_TO_MANY" -> OneToMany
                    else -> null
                }
            }
        }

        /**
         * Тип порядковой шкалы
         */
        sealed class ScaleType {

            /**
             * Линейный порядок
             */
            object Linear : ScaleType()

            /**
             * Частичный порядок
             */
            object Partial : ScaleType()

            companion object {

                fun valueOf(value: String) = when (value) {
                    "LINER" -> Linear
                    "PARTIAL" -> Partial
                    else -> null
                }
            }
        }
    }
}