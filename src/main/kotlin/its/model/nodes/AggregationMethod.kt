package its.model.nodes

/**
 * Метод агрегации результатов [BranchResult] в узлах [AggregationNode]
 */
enum class AggregationMethod {
    /**
     * Одновременные проверки по логическому И. Результат вычисляется так:
     * 1. [BranchResult.NULL] если все ветви такие
     * 2. [BranchResult.CORRECT] если все ветви либо [BranchResult.CORRECT] либо [BranchResult.NULL]
     * 3. [BranchResult.ERROR] иначе
     */
    AND {
        override val necessaryContinuationOutcomes = setOf(BranchResult.CORRECT)
    },

    /**
     * Одновременные проверки по логическому ИЛИ. Результат вычисляется так:
     * 1. [BranchResult.NULL] если все ветви такие
     * 2. [BranchResult.CORRECT] если есть хотя бы одна [BranchResult.CORRECT]
     * 3. [BranchResult.ERROR] иначе
     */
    OR {
        override val necessaryContinuationOutcomes = setOf(BranchResult.ERROR, BranchResult.NULL)
    },

    /**
     * Равновероятные гипотезы о рассуждениях. Результат вычисляется так:
     * 1. [BranchResult.CORRECT] если есть хотя бы одна [BranchResult.CORRECT]
     * 2. [BranchResult.ERROR] если есть хотя бы одна [BranchResult.ERROR]
     * 3. [BranchResult.NULL] иначе
     */
    HYP {
        override val necessaryContinuationOutcomes = setOf(BranchResult.NULL)
    },

    /**
     * Независимые (взаимоисключающие) проверки, только одна из которых может дать результат, не равный [BranchResult.NULL].
     * В качестве результата берется единственный [BranchResult], не равный [BranchResult.NULL]
     */
    MUTEX {
        override val necessaryContinuationOutcomes = emptySet<BranchResult>()
    },
    ;

    /**
     * По каким результатам агрегации обязательно должен быть выход для данного метода агрегации.
     */
    abstract val necessaryContinuationOutcomes: Set<BranchResult>

    companion object {
        @JvmStatic
        fun fromString(value: String): AggregationMethod? {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}