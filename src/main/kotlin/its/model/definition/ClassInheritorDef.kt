package its.model.definition

/**
 * Общий класс для классов и объектов - реализация наследования от классов
 */
sealed class ClassInheritorDef<Self : ClassInheritorDef<Self>> : DomainDefWithMeta<Self>() {

    protected abstract val parentClassName: String?
    abstract val definedPropertyValues: PropertyValueStatements<Self>

    /**
     * Для валидации - получить класс-родитель,
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownParentClass(results: DomainValidationResults): ClassDef? {
        if (parentClassName == null) return null

        val clazz = domainModel.classes.get(parentClassName!!)
        results.checkKnown(
            clazz != null,
            "No class definition '${parentClassName}' found to be defined as $name's parent class"
        )
        return clazz
    }

    /**
     * Для валидации - получить известную цепочку классов-родителей
     * (включая данный класс, если этот метод вызывается для класса)
     * добавляя сообщение о неизвестных родителях в [results], если такие есть
     */
    internal fun getKnownInheritanceLineage(results: DomainValidationResults): List<ClassDef> {
        val lineage = mutableListOf<ClassDef>()
        var p = if (this is ClassDef) this else getKnownParentClass(results)
        while (p != null) {
            lineage.add(p)
            p = p.getKnownParentClass(results)
            if (p === this) {
                results.invalid("$description is a supertype of itself (lineage is ${lineage.map { it.name }})")
                break
            }
        }
        return lineage
    }

    /**
     * Валидация - найти определение свойства по имени; любые ошибки кладутся в [results]
     */
    internal fun findPropertyDef(propertyName: String, results: DomainValidationResults): PropertyDef? {
        for (clazz in getKnownInheritanceLineage(results)) {
            val found = clazz.declaredProperties.get(propertyName)
            if (found != null) return found
        }
        return null
    }

    /**
     * Валидация - найти определение отношения по имени; любые ошибки кладутся в [results]
     */
    internal fun findRelationshipDef(
        relationshipName: String,
        results: DomainValidationResults
    ): RelationshipDef? {
        for (clazz in getKnownInheritanceLineage(results)) {
            val found = clazz.declaredRelationships.get(relationshipName)
            if (found != null) return found
        }
        return null
    }

    //---Операции (на валидном домене)---

    /**
     * Родительcкий класс данной сущности
     *
     * *(Для объектов данное свойство всегда присутствует (не null) - используй [ObjectDef.clazz])*
     */
    val parentClass: ClassDef?
        get() = getKnownParentClass(DomainValidationResultsThrowImmediately())

    /**
     * Получить цепочку классов объекта
     */
    fun getInheritanceLineage() = getKnownInheritanceLineage(DomainValidationResultsThrowImmediately())

    /**
     * Наследуется ли от класса
     */
    fun inheritsFrom(className: String) = getInheritanceLineage().any { it.name == className }

    /**
     * Наследуется ли от класса
     */
    fun inheritsFrom(classDef: ClassDef) = getInheritanceLineage().contains(classDef)

    /**
     * Все определенные для данной сущности свойства
     */
    val allProperties: List<PropertyDef>
        get() {
            val list = mutableListOf<PropertyDef>()
            getInheritanceLineage().forEach { list.addAll(it.declaredProperties) }
            return list
        }

    /**
     * Найти определение свойства по имени, с учетом наследования
     */
    fun findPropertyDef(propertyName: String) =
        findPropertyDef(propertyName, DomainValidationResultsThrowImmediately())

    /**
     * Все определенные для данной сущности отношения
     */
    val allRelationships: List<RelationshipDef>
        get() {
            val list = mutableListOf<RelationshipDef>()
            getInheritanceLineage().forEach { list.addAll(it.declaredRelationships) }
            return list
        }

    /**
     * Найти определение отношения по имени, с учетом наследования
     */
    fun findRelationshipDef(relationshipName: String) =
        findRelationshipDef(relationshipName, DomainValidationResultsThrowImmediately())

    /**
     * Получить значение свойства с учетом наследования
     * @throws DomainNonConformityException если такого свойства не существует
     */
    fun getPropertyValue(propertyName: String): Any {
        checkConforming(
            findPropertyDef(propertyName, DomainValidationResultsThrowImmediately()) != null,
            "No property $propertyName exists for $description"
        )
        val defined = definedPropertyValues.get(propertyName)
        if (defined != null) return defined.value
        for (clazz in getInheritanceLineage()) {
            val found = clazz.definedPropertyValues.get(propertyName)
            if (found != null) return found.value
        }
        nonConforming(
            "$description does not define a value for property '$propertyName'"
        )
    }
}