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
    internal fun getKnownSubjectClass(results: DomainValidationResults): Optional<ClassDef> {
        val clazzOpt = domain.classes.get(subjectClassName)
        results.checkKnown(
            clazzOpt.isPresent,
            "No class definition '$subjectClassName' found, while $description is said to be declared in it"
        )
        return clazzOpt
    }

    /**
     * Для валидации - получить известные классы-объекты отношения
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownObjectClasses(results: DomainValidationResults): List<Optional<ClassDef>> {
        return objectClassNames.map { className ->
            val clazzOpt = domain.classes.get(className)
            results.checkKnown(
                clazzOpt.isPresent,
                "No class definition '$className' found, while $description uses it as one of its object types"
            )
            clazzOpt
        }
    }

    /**
     * Для валидации - если текущее отношение имеет тип [DependantRelationshipKind],
     * то получить отношение, от которого зависит текущее, или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownBaseRelationship(results: DomainValidationResults): Optional<RelationshipDef> {
        if (kind !is DependantRelationshipKind) return Optional.empty()
        val baseRelOpt = kind.baseRelationshipRef.findIn(domain)
        results.checkKnown(
            baseRelOpt.isPresent,
            "No relationship definition '${kind.baseRelationshipRef}' " +
                    "found to be declared as a base relationship for $description"
        )
        return baseRelOpt
    }

    /**
     * Для валидации - получить известную цепочку зависимостей отношения (включая его самого)
     * добавляя сообщение о неизвестных отношениях в [results], если такие есть
     */
    internal fun getKnownDependencyLineage(results: DomainValidationResults): List<RelationshipDef> {
        val lineage = mutableListOf<RelationshipDef>()
        var p = Optional.of(this)
        while (p.isPresent) {
            lineage.add(p.get())
            p = p.get().getKnownBaseRelationship(results)
            if (p.isPresent && p.get() === this) {
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
        val subjectClassOpt = getKnownSubjectClass(results)
        val objectClassesOpt = getKnownObjectClasses(results)

        //Не переопределяет
        if (subjectClassOpt.isPresent) {
            val owner = subjectClassOpt.get()
            val lineage = owner.getKnownInheritanceLineage(results).minusElement(owner)
            for (parent in lineage) {
                if (parent.declaredRelationships.get(name).isPresent) {
                    results.invalid(
                        "relationship $name cannot be redeclared in ${owner.description}, " +
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
                    isBinary || kind.quantifier.isEmpty,
                    "Quantifiers are only allowed on binary relationships, but $description is not binary"
                )

                val scaleTypeOpt = kind.scaleType
                if (scaleTypeOpt.isPresent) {
                    val scaleType = scaleTypeOpt.get()
                    //Шкалу могут задавать только бинарные отношения между одинаковыми классами
                    results.checkValid(
                        isBinary && objectClassNames.first() == subjectClassName,
                        "$description has to be a binary relationship between the same types as it is declared as $scaleType"
                    )

                    //Соответсвие кватификаторов заданному типу шкалы
                    when (scaleType) {
                        BaseRelationshipKind.ScaleType.Linear -> results.checkValid(
                            kind.quantifier.isEmpty || kind.quantifier.get() == LinkQuantifier.OneToOne(),
                            "$description has to be quantified as one-to-one ( {1->1} ) as it is declared as $scaleType"
                        )

                        BaseRelationshipKind.ScaleType.Partial -> results.checkValid(
                            kind.quantifier.isEmpty || kind.quantifier.get().objCount == 1,
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
                    val dependenctRelationship = dependencyLineage.first()
                    val validTypes = when (kind.type) {
                        DependantRelationshipKind.Type.OPPOSITE -> this.isBinary && dependenctRelationship.isBinary
                                && this.subjectClassName == dependenctRelationship.objectClassNames.first()
                                && this.objectClassNames.first() == dependenctRelationship.subjectClassName

                        DependantRelationshipKind.Type.TRANSITIVE -> this.isBinary && dependenctRelationship.isBinary
                                && this.subjectClassName == dependenctRelationship.subjectClassName
                                && this.objectClassNames.first() == dependenctRelationship.objectClassNames.first()

                        DependantRelationshipKind.Type.BETWEEN,
                        DependantRelationshipKind.Type.CLOSER,
                        DependantRelationshipKind.Type.FURTHER -> this.isTernary && dependenctRelationship.isBinary
                                && this.subjectClassName == dependenctRelationship.subjectClassName
                                && this.objectClassNames[0] == dependenctRelationship.objectClassNames.first()
                                && this.objectClassNames[1] == dependenctRelationship.objectClassNames.first()
                    }

                    results.checkValid(
                        validTypes,
                        "typing of $description isn't valid " +
                                "given the typing of its dependency relationship, ${dependenctRelationship.description}"
                    )

                    //"Транзитивность" (?) и "между", "ближе", "дальше" можно рассчитать только на шкалах
                    val baseRelationship = dependencyLineage.last()
                    if (baseRelationship.kind is BaseRelationshipKind) {
                        results.checkValid(
                            kind.type == DependantRelationshipKind.Type.OPPOSITE ||
                                    baseRelationship.isScalar,
                            "${kind.type} relationships can only be based on scalar relationships, " +
                                    "but $description is based on ${baseRelationship.description}, which is not scalar"
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
        get() = getKnownSubjectClass(DomainValidationResultsThrowImmediately()).get()

    /**
     * Классы-объекты отношения
     */
    val objectClasses: List<ClassDef>
        get() = getKnownObjectClasses(DomainValidationResultsThrowImmediately()).map { it.get() }

    /**
     * Задает ли отношение шкалу
     */
    val isScalar: Boolean
        get() = kind is BaseRelationshipKind && kind.scaleType.isPresent

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
    val baseRelationship: Optional<RelationshipDef>
        get() = getKnownBaseRelationship(DomainValidationResultsThrowImmediately())

    /**
     * Фактический квантификатор (с учетом зависимостей и шкалы)
     */
    val effectiveQuantifier: LinkQuantifier
        get() {
            when (kind) {
                is BaseRelationshipKind -> {
                    if (kind.quantifier.isPresent) return kind.quantifier.get()
                    if (kind.scaleType.isPresent) {
                        val scaleType = kind.scaleType.get()
                        return when (scaleType) {
                            BaseRelationshipKind.ScaleType.Linear -> LinkQuantifier.OneToOne()
                            BaseRelationshipKind.ScaleType.Partial -> LinkQuantifier.ManyToOne()
                        }
                    }
                    return LinkQuantifier.ManyToMany()
                }

                is DependantRelationshipKind -> {
                    if (kind.type == DependantRelationshipKind.Type.OPPOSITE)
                        return baseRelationship.get().effectiveQuantifier.reversed
                    return LinkQuantifier.ManyToMany()
                }
            }
        }
}

class RelationshipContainer(clazz: ClassDef) : ChildDefContainer<RelationshipDef, ClassDef>(clazz)

class RelationshipRef(
    val className: String,
    val relationshipName: String,
) : DomainRef<RelationshipDef> {
    override fun findIn(domain: Domain): Optional<RelationshipDef> {
        val classOpt = ClassRef(className).findIn(domain)
        if (!classOpt.isPresent) return Optional.empty()
        return classOpt.get().declaredRelationships.get(relationshipName)
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