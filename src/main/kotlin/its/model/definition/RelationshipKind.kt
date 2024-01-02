package its.model.definition

import java.util.*

//Вспомогательные классы для RelationshipDef

/**
 * Тип отношения [RelationshipDef]
 */
sealed interface RelationshipKind {
    val isBase: Boolean
        get() = this is BaseRelationshipKind
}

/**
 * Независимое отношение
 */
class BaseRelationshipKind(
    val scaleType: ScaleType? = null,
    val quantifier: LinkQuantifier? = null,
) : RelationshipKind {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseRelationshipKind

        if (scaleType != other.scaleType) return false
        if (quantifier != other.quantifier) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, scaleType, quantifier)
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
}

/**
 * Квантификатор отношения (какое кол-во связей допустимо)
 */
data class LinkQuantifier(
    val subjCount: Int = ANY_COUNT,
    val objCount: Int = ANY_COUNT,
) {
    val reversed: LinkQuantifier
        get() = LinkQuantifier(objCount, subjCount)

    companion object {
        @JvmStatic
        val ANY_COUNT = Int.MAX_VALUE

        @JvmStatic
        fun OneToOne() = LinkQuantifier(1, 1)

        @JvmStatic
        fun OneToMany() = LinkQuantifier(1, ANY_COUNT)

        @JvmStatic
        fun ManyToOne() = LinkQuantifier(ANY_COUNT, 1)

        @JvmStatic
        fun ManyToMany() = LinkQuantifier(ANY_COUNT, ANY_COUNT)
    }
}


/**
 * Зависимое (вычисляемое) отношение
 */
class DependantRelationshipKind(
    val type: Type,
    val baseRelationshipRef: RelationshipRef,
) : RelationshipKind {

    /**
     * Тип зависимости вычисляемого отношения
     */
    enum class Type {
        OPPOSITE,
        TRANSITIVE,
        BETWEEN,
        CLOSER,
        FURTHER,
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DependantRelationshipKind

        if (type != other.type) return false
        if (baseRelationshipRef != other.baseRelationshipRef) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, type, baseRelationshipRef)
    }
}