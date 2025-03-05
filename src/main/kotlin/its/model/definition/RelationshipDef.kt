package its.model.definition

import java.util.*

/**
 * Определение отношения между объектами в домене
 */
class RelationshipDef(
    val subjectClassName: String,
    override val name: String,
    val objectClassNames: List<String>,
    val kind: RelationshipKind = BaseRelationshipKind(),
) : DomainDefWithMeta<RelationshipDef>() {

    override val description = "relationship ${subjectClassName}->$name(${objectClassNames.joinToString(", ")})"
    override val reference = RelationshipRef(subjectClassName, name)

    /**
     * Для валидации - получить известный класс-субъект отношения
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownSubjectClass(results: DomainValidationResults): ClassDef? {
        val clazz = domainModel.classes.get(subjectClassName)
        results.checkKnown(
            clazz != null,
            "No class definition '$subjectClassName' found, while $description is said to be declared in it"
        )
        return clazz
    }

    /**
     * Для валидации - получить известные классы-объекты отношения
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownObjectClasses(results: DomainValidationResults): List<ClassDef?> {
        return objectClassNames.map { className ->
            val clazz = domainModel.classes.get(className)
            results.checkKnown(
                clazz != null,
                "No class definition '$className' found, while $description uses it as one of its object types"
            )
            clazz
        }
    }

    /**
     * Для валидации - если текущее отношение имеет тип [DependantRelationshipKind],
     * то получить отношение, от которого зависит текущее, или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownBaseRelationship(results: DomainValidationResults): RelationshipDef? {
        if (kind !is DependantRelationshipKind) return null
        val baseRelationship = kind.baseRelationshipRef.findIn(domainModel)
        results.checkKnown(
            baseRelationship != null,
            "No relationship definition '${kind.baseRelationshipRef}' " +
                    "found to be declared as a base relationship for $description"
        )
        return baseRelationship
    }

    /**
     * Для валидации - получить известную цепочку зависимостей отношения (включая его самого)
     * добавляя сообщение о неизвестных отношениях в [results], если такие есть
     */
    internal fun getKnownDependencyLineage(results: DomainValidationResults): List<RelationshipDef> {
        val lineage = mutableListOf<RelationshipDef>()
        var p: RelationshipDef? = this
        while (p != null) {
            lineage.add(p)
            p = p.getKnownBaseRelationship(results)
            if (p === this) {
                results.invalid("$description is cyclically dependant on itself (lineage is ${lineage.map { it.name }})")
                break
            }
        }
        return lineage
    }

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        results.checkValid(
            objectClassNames.size > 0,
            "$description has no object types"
        )

        //Существование классов
        val subjectClass = getKnownSubjectClass(results)
        val objectClasses = getKnownObjectClasses(results)

        //Не переопределяет
        if (subjectClass != null) {
            val lineage = subjectClass.getKnownInheritanceLineage(results).minusElement(subjectClass)
            for (parent in lineage) {
                if (parent.declaredRelationships.get(name) != null) {
                    results.invalid(
                        "relationship $name cannot be redeclared in ${subjectClass.description}, " +
                                "as a relationship with the same name is already declared in superclass ${parent.description}."
                    )
                    break
                }
            }
        }

        //проверки в зависимости от типа отношения
        when (kind) {
            is BaseRelationshipKind -> {
                //квантификаторы могут быть только у бинарных отношений
                results.checkValid(
                    isBinary || kind.quantifier == null,
                    "Quantifiers are only allowed on binary relationships, but $description is not binary"
                )

                val scaleType = kind.scaleType
                if (scaleType != null) {
                    //Шкалу могут задавать только бинарные отношения между одинаковыми классами
                    results.checkValid(
                        isBinary && objectClassNames.first() == subjectClassName,
                        "$description has to be a binary relationship between the same types as it is declared as $scaleType"
                    )

                    //Соответсвие кватификаторов заданному типу шкалы
                    when (scaleType) {
                        BaseRelationshipKind.ScaleType.Linear -> results.checkValid(
                            kind.quantifier == null || kind.quantifier == LinkQuantifier.OneToOne(),
                            "$description has to be quantified as one-to-one ( {1->1} ) as it is declared as $scaleType"
                        )

                        BaseRelationshipKind.ScaleType.Partial -> results.checkValid(
                            kind.quantifier == null || kind.quantifier.objCount == 1,
                            "$description has to be quantified as many-to-one ( {...->1} ) as it is declared as $scaleType"
                        )
                    }
                }
            }

            is DependantRelationshipKind -> {
                //Существование и нецикличность зависимостей
                val dependencyLineage = getKnownDependencyLineage(results).minusElement(this)

                if (dependencyLineage.isNotEmpty()) {
                    //Соответствие типов с отношением, от которого зависит
                    val baseRelationship = dependencyLineage.first()
                    val validTypes = when (kind.type) {
                        DependantRelationshipKind.Type.OPPOSITE -> this.isBinary && baseRelationship.isBinary
                                && this.subjectClassName == baseRelationship.objectClassNames.first()
                                && this.objectClassNames.first() == baseRelationship.subjectClassName

                        DependantRelationshipKind.Type.TRANSITIVE -> this.isBinary && baseRelationship.isBinary
                                && this.subjectClassName == baseRelationship.subjectClassName
                                && this.objectClassNames.first() == baseRelationship.objectClassNames.first()

                        DependantRelationshipKind.Type.BETWEEN,
                        DependantRelationshipKind.Type.CLOSER,
                        DependantRelationshipKind.Type.FURTHER -> this.isTernary && baseRelationship.isBinary
                                && this.subjectClassName == baseRelationship.subjectClassName
                                && this.objectClassNames[0] == baseRelationship.objectClassNames.first()
                                && this.objectClassNames[1] == baseRelationship.objectClassNames.first()
                    }

                    results.checkValid(
                        validTypes,
                        "typing of $description isn't valid " +
                                "given the typing of its base relationship, ${baseRelationship.description}"
                    )

                    //"Транзитивность" (?) и "между", "ближе", "дальше" можно рассчитать только на шкалах
                    val rootRelationship = dependencyLineage.last()
                    if (rootRelationship.kind is BaseRelationshipKind) {
                        results.checkValid(
                            kind.type == DependantRelationshipKind.Type.OPPOSITE ||
                                    rootRelationship.isScalar,
                            "${kind.type} relationships can only depend on scalar relationships, " +
                                    "but $description is depends on ${rootRelationship.description}, which is not scalar"
                        )
                    }
                }
            }
        }
    }

    //----------------------------------

    override fun plainCopy() = RelationshipDef(subjectClassName, name, objectClassNames, kind)

    override fun mergeEquals(other: RelationshipDef): Boolean {
        if (!super.mergeEquals(other)) return false
        return subjectClassName == other.subjectClassName
                && name == other.name
                && objectClassNames == other.objectClassNames
                && kind == other.kind
    }

    //---Операции (на валидном домене)---

    /**
     * Класс-субъект отношения
     */
    val subjectClass: ClassDef
        get() = getKnownSubjectClass(DomainValidationResultsThrowImmediately())!!

    /**
     * Классы-объекты отношения
     */
    val objectClasses: List<ClassDef>
        get() = getKnownObjectClasses(DomainValidationResultsThrowImmediately()).requireNoNulls()

    /**
     * Задает ли отношение шкалу
     */
    val isScalar: Boolean
        get() = kind is BaseRelationshipKind && kind.scaleType != null

    /**
     * Является ли отношение бинарным
     */
    val isBinary: Boolean
        get() = objectClassNames.size == 1

    /**
     * Является ли отношение тернарным
     */
    val isTernary: Boolean
        get() = objectClassNames.size == 2

    /**
     * Является ли отношение н-арным (более чем бинарным)
     */
    val isNAry: Boolean
        get() = objectClassNames.size > 1

    /**
     * Является ли отношение неупорядоченным
     */
    val isUnordered: Boolean
        get() = objectClassNames.toSet().size == 1 && !(kind is DependantRelationshipKind && kind.type in setOf(
            DependantRelationshipKind.Type.CLOSER,
            DependantRelationshipKind.Type.FURTHER,
        ))

    /**
     * Отношение, от которого зависит текущее (если текущее имеет тип [DependantRelationshipKind])
     */
    val baseRelationship: RelationshipDef?
        get() = getKnownBaseRelationship(DomainValidationResultsThrowImmediately())

    /**
     * Фактический квантификатор (с учетом зависимостей и шкалы)
     */
    val effectiveQuantifier: LinkQuantifier
        get() {
            when (kind) {
                is BaseRelationshipKind -> {
                    if (kind.quantifier != null) return kind.quantifier
                    if (kind.scaleType != null) {
                        val scaleType = kind.scaleType
                        return when (scaleType) {
                            BaseRelationshipKind.ScaleType.Linear -> LinkQuantifier.OneToOne()
                            BaseRelationshipKind.ScaleType.Partial -> LinkQuantifier.ManyToOne()
                        }
                    }
                    return LinkQuantifier.ManyToMany()
                }

                is DependantRelationshipKind -> {
                    if (kind.type == DependantRelationshipKind.Type.OPPOSITE)
                        return baseRelationship!!.effectiveQuantifier.reversed
                    return LinkQuantifier.ManyToMany()
                }
            }
        }

    val effectiveParams: ParamsDecl
        get() = when (kind) {
            is BaseRelationshipKind -> kind.paramsDecl
            is DependantRelationshipKind -> baseRelationship!!.effectiveParams
        }
}

class RelationshipContainer(clazz: ClassDef) : ChildDefContainer<RelationshipDef, ClassDef>(clazz)

class RelationshipRef(
    val className: String,
    val relationshipName: String,
) : DomainRef<RelationshipDef> {
    override fun findIn(domainModel: DomainModel): RelationshipDef? {
        val clazz = ClassRef(className).findIn(domainModel) ?: return null
        return clazz.declaredRelationships.get(relationshipName)
    }

    override fun toString() = "$className->$relationshipName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RelationshipRef) return false

        if (className != other.className) return false
        if (relationshipName != other.relationshipName) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, className, relationshipName)
    }
}