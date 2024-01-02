package its.model.definition.types

/**
 * Диапазон числовых значений
 */
sealed class Range {
    /**
     * Содержит ли диапазон число
     */
    abstract fun contains(v: Number): Boolean

    /**
     * Входит ли диапазон [other] в текущий диапазон (полностью содержится в нем)
     */
    abstract fun contains(other: Range): Boolean

    /**
     * Строковое представление диапазона в виде модификатора для типа
     */
    abstract val modString: String

    protected fun Double.mapNum() = if (this.rem(1) == 0.0) this.toInt() else this
}

/**
 * Любое число
 */
data object AnyNumber : Range() {
    override fun contains(v: Number): Boolean = true
    override fun contains(other: Range): Boolean = true

    override val modString: String
        get() = ""
}

/**
 * Дискретный диапазон (набор допустимых значений)
 */
class DiscreteRange(val values: Set<Double>) : Range() {
    override fun contains(v: Number): Boolean {
        return values.any { it.compareTo(v.toDouble()) == 0 }
    }

    override fun contains(other: Range): Boolean {
        if (other is ContinuousRange)
            return other.boundaries.first == other.boundaries.second
                    && this.contains(other.boundaries.first)

        return other is DiscreteRange && other.values == this.values
    }

    override val modString: String
        get() = "{${values.map { it.mapNum() }.joinToString(", ")}}"
}

/**
 * Непрерывный диапазон (между двумя значениями)
 */
class ContinuousRange(boundaries: Pair<Double, Double>) : Range() {
    val boundaries: Pair<Double, Double>

    init {
        if (boundaries.first < boundaries.second)
            this.boundaries = boundaries.first to boundaries.second
        else
            this.boundaries = boundaries.second to boundaries.first
    }

    override fun contains(v: Number): Boolean {
        return v.toDouble() in boundaries.first..boundaries.second
    }

    override fun contains(other: Range): Boolean {
        return when (other) {
            AnyNumber -> boundaries.first == Double.NEGATIVE_INFINITY && boundaries.second == Double.POSITIVE_INFINITY
            is ContinuousRange -> this.boundaries == other.boundaries
            is DiscreteRange -> other.values.all { this.contains(it) }
        }
    }

    override val modString: String
        get() = "[${if (boundaries.first.isFinite()) boundaries.first.mapNum() else ""}" +
                ", ${if (boundaries.second.isFinite()) boundaries.second.mapNum() else ""}]"
}

