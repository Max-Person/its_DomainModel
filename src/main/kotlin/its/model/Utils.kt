package its.model

/**
 * элиас для проверки на ненулловость
 */
val Any?.isPresent
    get() = this != null

/**
 * элиас для проверки на нулловость
 */
val Any?.isEmpty
    get() = this == null

fun <V> V?.nullCheck(message: String): V {
    if (this == null) throw IllegalArgumentException(message)
    return this
}