package its.model.models

sealed interface Range {
    fun contains(v: Number): Boolean
}

data object AnyNumber : Range {
    override fun contains(v: Number): Boolean = true
}

class DiscreteRange(val values: List<Double>) : Range {
    override fun contains(v: Number): Boolean {
        return values.any { it.compareTo(v.toDouble()) == 0 }
    }
}

class ContinuousRange(boundaries: Pair<Double, Double>) : Range {
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
}

