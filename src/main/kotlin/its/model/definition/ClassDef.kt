package its.model.definition

import java.util.*

/**
 * Класс в домене ([DomainModel])
 */
class ClassDef(
    override val name: String,
    val parentName: String?,
) : ClassInheritorDef<ClassDef>() {

    override val parentClassName: String?
        get() = parentName

    override val description = "class $name"
    override val reference = ClassRef(name)

    /**
     * Определяемые данным классом свойства
     */
    val declaredProperties = PropertyContainer(this)

    /**
     * Определяемые данным классом отношения
     */
    val declaredRelationships = RelationshipContainer(this)

    /**
     * Значения для свойств, указанные в данном классе
     */
    override val definedPropertyValues = ClassPropertyValueStatements(this)


    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        //Существование родителя и ацикличность зависимостей
        getKnownInheritanceLineage(results)

        declaredProperties.validate(results)
        declaredRelationships.validate(results)
        definedPropertyValues.validate(results)
    }

    //----------------------------------

    override fun plainCopy() = ClassDef(name, parentName)

    override fun mergeEquals(other: ClassDef): Boolean {
        if (!super.mergeEquals(other)) return false
        return name == other.name
                && parentName == other.parentName
    }

    override fun addMerge(other: ClassDef) {
        super.addMerge(other)
        declaredProperties.addAllMerge(other.declaredProperties)
        declaredRelationships.addAllMerge(other.declaredRelationships)

        definedPropertyValues.addAll(other.definedPropertyValues)
    }

    override val isEmpty: Boolean
        get() = super.isEmpty
                && declaredProperties.isEmpty()
                && declaredRelationships.isEmpty()
                && definedPropertyValues.isEmpty()

    override fun subtract(other: ClassDef) {
        super.subtract(other)
        declaredProperties.subtract(other.declaredProperties)
        declaredRelationships.subtract(other.declaredRelationships)

        definedPropertyValues.subtract(other.definedPropertyValues)
    }

    //---Операции (на валидном домене)---

    /**
     * Является ли подтипом класса
     *
     * (alias для [inheritsFrom])
     */
    fun isSubclassOf(className: String) = inheritsFrom(className)

    /**
     * @see isSubclassOf
     */
    fun isSubclassOf(classDef: ClassDef) = inheritsFrom(classDef)

    /**
     * Прямые классы-наследники данного класса
     */
    val children: List<ClassDef>
        get() = domainModel.classes.filter { clazz -> clazz.parentName == this.name }

    /**
     * Прямые экземпляры данного класса (наследуются напрямую от этого класса)
     */
    val directInstances: List<ObjectDef>
        get() = domainModel.objects.filter { obj -> obj.className == this.name }


    /**
     * Объекты экземпляры данного класса
     */
    val instances: List<ObjectDef>
        get() = domainModel.objects.filter { obj -> obj.isInstanceOf(this.name) }

    /**
     * Является ли класс конкретным (имеет ли объекты-экземпляры)
     */
    val isConcrete: Boolean
        get() = domainModel.objects.any { obj -> obj.className == this.name } //есть экземпляры

    /**
     * Все отношения данного класса, с помощью которых возможна проекция;
     * Отношение позволяет проекцию, если является бинарным и единично квантифицирована на стороне субъекта ("Один к ...")
     */
    val projectionRelationships: List<RelationshipDef>
        get() = allRelationships.filter { it.isBinary && it.effectiveQuantifier.subjCount == 1 }

    /**
     * Отношение, с помощью которого данный класс может быть спроецирован на [other], с учетом наследования
     * @throws ModelMisuseException если такого отношения нет, или оно не одно
     * @see canBeProjectedOnto
     */
    fun getProjectionRelationship(other: ClassDef): RelationshipDef {
        val fittingRelationships = projectionRelationships.filter { it.objectClasses.first().isSubclassOf(other) }
        preventMisuse(
            fittingRelationships.isNotEmpty(),
            "No projection relationship exists from $description onto ${other.description}"
        )
        preventMisuse(
            fittingRelationships.size < 2,
            "Ambiguous projection from $description onto ${other.description} - " +
                    "can be done with all of the following relationships: "
        )
        return fittingRelationships.single()
    }

    /**
     * Может ли данный класс может быть спроецирован на [other], с учетом наследования
     * @see getProjectionRelationship
     */
    fun canBeProjectedOnto(other: ClassDef): Boolean {
        return try {
            getProjectionRelationship(other)
            true
        } catch (e: ModelMisuseException) {
            false
        }
    }

    /**
     * Список всех классов, на которые может быть спроецирован текущий;
     *
     * В данный список попадают только самые дочерние классы,
     * но данный класс также может быть спроецирован на их родителей,
     * поэтому для фактической проверки лучше использовать [canBeProjectedOnto]
     */
    val projectionClasses: List<ClassDef>
        get() = projectionRelationships.map { it.objectClasses.first() }.filter { this.canBeProjectedOnto(it) }

}

class ClassContainer(domainModel: DomainModel) : RootDefContainer<ClassDef>(domainModel) {
    override fun addNew(def: ClassDef): ClassDef {
        return super.addNew(def).also { domainModel.separateClassPropertyValues.claimIfPresent(it) }
    }
}

class ClassRef(
    val className: String,
) : DomainRef<ClassDef> {
    override fun findIn(domainModel: DomainModel) = domainModel.classes.get(className)
    override fun toString() = "class $className"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClassRef) return false

        if (className != other.className) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, className)
    }
}