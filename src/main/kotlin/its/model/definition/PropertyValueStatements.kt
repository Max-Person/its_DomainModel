package its.model.definition

import java.util.*

/**
 * Утверждение о значении свойства
 */
abstract class PropertyValueStatement<Owner>(
    override val owner: Owner,
    val propertyName: String,
    val value: Any,
) : Statement<Owner>() where Owner : DomainDef<Owner> {

    override val description = "statement ${owner.name}.$propertyName = $value"

    //FIXME подумать над тем, не вынести ли это в интерфейс для классов и объектов
    protected abstract fun Owner.getPropertyValues(): PropertyValueStatements<Owner>
    protected abstract fun Owner.findPropertyDef(
        propertyName: String,
        results: DomainValidationResults
    ): Optional<PropertyDef>

    protected abstract fun PropertyDef.PropertyKind.fits(owner: Owner): Boolean

    override fun validate(results: DomainValidationResults) {
        //Существование свойства
        val propertyOpt = owner.findPropertyDef(propertyName, results)
        if (propertyOpt.isEmpty) {
            results.unknown(
                "No property definition '$propertyName' found to define the value $value in ${owner.description}"
            )
            return
        }
        val property = propertyOpt.get()

        //Соответствие вида свойства месту определения (внутри класса/объекта)
        results.checkValid(
            property.kind.fits(owner),
            "Cannot define ${property.description} in ${owner.description}"
        )

        try {
            //Соответствие типа значению
            results.checkValid(
                property.type.fits(value),
                "Type of ${property.description} (${property.type}) does not " +
                        "fit the value '$value' defined in ${owner.description}"
            )
        } catch (e: DomainDefinitionException) { //type.fits может выкинуть исключение для неизвестного енама
            results.add(e)
        }

        //Переопределение значений выше? Но можно сказать что это разрешено
        //Проверка на количество значений не выполняется, т.к. PropertyValueStatements гарантирует уникальность
    }
}

class ClassPropertyValueStatement(
    owner: ClassDef,
    propertyName: String,
    value: Any,
) : PropertyValueStatement<ClassDef>(owner, propertyName, value) {
    override fun ClassDef.getPropertyValues() = definedPropertyValues
    override fun ClassDef.findPropertyDef(
        propertyName: String,
        results: DomainValidationResults
    ): Optional<PropertyDef> {
        return findPropertyDef(propertyName, results)
    }

    override fun PropertyDef.PropertyKind.fits(owner: ClassDef) = this == PropertyDef.PropertyKind.CLASS

    override fun copyForOwner(owner: ClassDef) = ClassPropertyValueStatement(owner, propertyName, value)
}


class ObjectPropertyValueStatement(
    owner: ObjectDef,
    propertyName: String,
    value: Any,
) : PropertyValueStatement<ObjectDef>(owner, propertyName, value) {
    override fun ObjectDef.getPropertyValues() = definedPropertyValues
    override fun ObjectDef.findPropertyDef(
        propertyName: String,
        results: DomainValidationResults
    ): Optional<PropertyDef> {
        return findPropertyDef(propertyName, results)
    }

    override fun PropertyDef.PropertyKind.fits(owner: ObjectDef) = this == PropertyDef.PropertyKind.OBJECT

    override fun copyForOwner(owner: ObjectDef) = ObjectPropertyValueStatement(owner, propertyName, value)
}


abstract class PropertyValueStatements<Owner>(
    owner: Owner,
) : Statements<Owner, PropertyValueStatement<Owner>>(owner) where Owner : DomainDef<Owner> {
    protected val map = mutableMapOf<String, PropertyValueStatement<Owner>>()
    override fun iterator() = map.values.iterator()
    override fun addToInner(statement: PropertyValueStatement<Owner>) {
        val existing = get(statement.propertyName)
        checkValid(
            existing.map { it.value == statement.value }.orElse(true),
            "cannot add ${statement.description}, " +
                    "because ${owner.description} already defines a value '${existing.map { it.value }}'" +
                    "for property '${statement.propertyName}'"
        )
        map[statement.propertyName] = statement
    }

    fun get(propertyName: String): Optional<PropertyValueStatement<Owner>> {
        return Optional.ofNullable(map[propertyName])
    }

    override val size: Int
        get() = map.size

    override fun contains(element: PropertyValueStatement<Owner>): Boolean {
        return map.containsKey(element.propertyName) && map[element.propertyName] == element
    }

    override fun containsAll(elements: Collection<PropertyValueStatement<Owner>>) = elements.all { contains(it) }
    override fun isEmpty() = map.isEmpty()
}


class ClassPropertyValueStatements(
    owner: ClassDef
) : PropertyValueStatements<ClassDef>(owner) {
    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        if (!owner.isConcrete) return
        //Определяет все нужные свойства
        val topDownLineage = owner.getKnownInheritanceLineage(results).minusElement(owner).reversed()
        val undefinedClassProperties = mutableSetOf<PropertyDef>()
        for (clazz in topDownLineage) {
            undefinedClassProperties.addAll(clazz.declaredProperties.filter { it.kind == PropertyDef.PropertyKind.CLASS })
            undefinedClassProperties.removeIf { clazz.definedPropertyValues.get(it.name).isPresent }
        }
        for (undefinedProperty in undefinedClassProperties) {
            results.checkKnown(
                get(undefinedProperty.name).isPresent,
                "${owner.description} does not define a value for ${undefinedProperty.description}"
            )
        }
    }
}

class ObjectPropertyValueStatements(
    owner: ObjectDef
) : PropertyValueStatements<ObjectDef>(owner) {
    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        //Определяет все нужные свойства
        val lineage = owner.getKnownInheritanceLineage(results)
        for (clazz in lineage) {
            val objProperties = clazz.declaredProperties.filter { it.kind == PropertyDef.PropertyKind.OBJECT }
            for (undefinedProperty in objProperties) {
                results.checkKnown(
                    get(undefinedProperty.name).isPresent,
                    "${owner.description} does not define a value for ${undefinedProperty.description}"
                )
            }
        }
    }
}