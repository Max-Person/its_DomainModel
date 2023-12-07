package its.model.definition

import java.util.*

/**
 * Утверждение о значении свойства
 */
class PropertyValueStatement<Owner : ClassInheritorDef<Owner>>(
    override val owner: Owner,
    val propertyName: String,
    val value: Any,
) : Statement<Owner>() {
    override fun copyForOwner(owner: Owner) = PropertyValueStatement(owner, propertyName, value)

    override val description = "statement ${owner.name}.$propertyName = $value"

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
                property.type.fits(value, domain),
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

typealias ClassPropertyValueStatement = PropertyValueStatement<ClassDef>
typealias ObjectPropertyValueStatement = PropertyValueStatement<ObjectDef>


class PropertyValueStatements<Owner : ClassInheritorDef<Owner>>(
    owner: Owner,
) : Statements<Owner, PropertyValueStatement<Owner>>(owner) {
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

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        if (owner is ClassDef && !owner.isConcrete) return
        //Определяет все нужные свойства
        val topDownLineage = owner.getKnownInheritanceLineage(results).reversed()
        val undefinedClassProperties = mutableSetOf<PropertyDef>()
        for (clazz in topDownLineage) {
            undefinedClassProperties.addAll(clazz.declaredProperties.filter { it.kind.fits(owner) })
            undefinedClassProperties.removeIf {
                it.kind.fits(clazz) && clazz.definedPropertyValues.get(it.name).isPresent
            }
        }
        for (undefinedProperty in undefinedClassProperties) {
            results.checkKnown(
                get(undefinedProperty.name).isPresent,
                "${owner.description} does not define a value for ${undefinedProperty.description}"
            )
        }
    }

    override val size: Int
        get() = map.size

    override fun contains(element: PropertyValueStatement<Owner>): Boolean {
        return map.containsKey(element.propertyName) && map[element.propertyName] == element
    }

    override fun containsAll(elements: Collection<PropertyValueStatement<Owner>>) = elements.all { contains(it) }
    override fun isEmpty() = map.isEmpty()
}

typealias ClassPropertyValueStatements = PropertyValueStatements<ClassDef>
typealias ObjectPropertyValueStatements = PropertyValueStatements<ObjectDef>