package its.model.definition

import its.model.models.RelationshipModel
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
    val scaleType: Optional<RelationshipModel.ScaleType> = Optional.empty(),
    val quantifier: Optional<LinkQuantifier> = Optional.empty(),
) : RelationshipKind

/**
 * Квантификатор отношения (какое кол-во связей допустимо)
 */
data class LinkQuantifier(
    val subjCount: Int = ANY_COUNT,
    val objCount: Int = ANY_COUNT,
) {
    companion object {
        @JvmStatic
        val ANY_COUNT = -1

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
}