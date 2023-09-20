package ru.compprehension.its.model.nodes

enum class LogicalOp {
    AND,
    OR;

    companion object _static {
        @JvmStatic
        fun fromString(value: String) = when (value.uppercase()) {
            "AND" -> AND
            "OR" -> OR
            else -> null
        }
    }
}