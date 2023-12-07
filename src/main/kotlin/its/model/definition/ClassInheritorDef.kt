package its.model.definition

import java.util.*

/**
 * Общий класс для классов и объектов - реализация наследования от классов
 */
sealed class ClassInheritorDef<Self : ClassInheritorDef<Self>> : DomainDefWithMeta<Self>() {

    protected abstract val parentClassName: Optional<String>
    abstract val definedPropertyValues: PropertyValueStatements<Self>

    /**
     * Для валидации - получить класс-родитель,
     * или добавить сообщение о его неизвестности в [results]
     */
    internal fun getKnownParentClass(results: DomainValidationResults): Optional<ClassDef> {
        if (!parentClassName.isPresent) return Optional.empty()

        val clazzOpt = domain.classes.get(parentClassName.get())
        results.checkKnown(
            clazzOpt.isPresent,
            "No class definition '${parentClassName.get()}' found to be defined as $name's parent class"
        )
        return clazzOpt
    }

    /**
     * Для валидации - получить известную цепочку классов-родителей
     * (включая данный класс, если этот метод вызывается для класса)
     * добавляя сообщение о неизвестных родителях в [results], если такие есть
     */
    internal fun getKnownInheritanceLineage(results: DomainValidationResults): List<ClassDef> {
        val lineage = mutableListOf<ClassDef>()
        var p = if (this is ClassDef) Optional.of(this) else getKnownParentClass(results)
        while (p.isPresent) {
            lineage.add(p.get())
            p = p.get().getKnownParentClass(results)
            if (p.isPresent && p.get() === this) {
                results.invalid("$description is a supertype of itself (lineage is ${lineage.map { it.name }})")
                break
            }
        }
        return lineage
    }

    /**
     * Валидация - найти определение свойства по имени; любые ошибки кладутся в [results]
     */
    internal fun findPropertyDef(propertyName: String, results: DomainValidationResults): Optional<PropertyDef> {
        for (clazz in getKnownInheritanceLineage(results)) {
            val found = clazz.declaredProperties.get(propertyName)
            if (found.isPresent) return found
        }
        return Optional.empty()
    }

    /**
     * Валидация - найти определение отношения по имени; любые ошибки кладутся в [results]
     */
    internal fun findRelationshipDef(
        relationshipName: String,
        results: DomainValidationResults
    ): Optional<RelationshipDef> {
        for (clazz in getKnownInheritanceLineage(results)) {
            val found = clazz.declaredRelationships.get(relationshipName)
            if (found.isPresent) return found
        }
        return Optional.empty()
    }

    //---Операции (на валидном домене)---

    /**
     * Родительcкий класс данной сущности
     *
     * *(Для объектов данный Optional всегда заполнен - используй [ObjectDef.clazz])*
     */
    val parentClass: Optional<ClassDef>
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
     * Все определенные для данной сущности отношения
     */
    val allRelationships: List<RelationshipDef>
        get() {
            val list = mutableListOf<RelationshipDef>()
            getInheritanceLineage().forEach { list.addAll(it.declaredRelationships) }
            return list
        }

    /**
     * Получить значение свойства с учетом наследования
     * @throws DomainNonConformityException если такого свойства не существует
     */
    fun getPropertyValue(propertyName: String): Any {
        checkConforming(
            findPropertyDef(propertyName, DomainValidationResultsThrowImmediately()).isPresent,
            "No property $propertyName exists for $description"
        )
        val definedOpt = definedPropertyValues.get(propertyName)
        if (definedOpt.isPresent) return definedOpt
        for (clazz in getInheritanceLineage()) {
            val found = clazz.definedPropertyValues.get(propertyName)
            if (found.isPresent) return found.get()
        }
        throw ThisShouldNotHappen()
    }
}