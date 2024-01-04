package its.model.definition

import its.model.definition.types.EnumType

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
        val property = owner.findPropertyDef(propertyName, results)
        if (property == null) {
            results.unknown(
                "No property definition '$propertyName' found to define the value $value in ${owner.description}"
            )
            return
        }

        //Соответствие вида свойства месту определения (внутри класса/объекта)
        results.checkValid(
            property.kind.fits(owner),
            "Cannot define ${property.description} in ${owner.description}"
        )

        if (property.type is EnumType && !property.type.exists(domain)) {
            results.unknown(
                "No enum definition '${property.type.enumName}' found to check if a value fits to a enum type"
            )
        } else {
            //Соответствие типа значению
            results.checkValid(
                property.type.fits(value, domain),
                "Type of ${property.description} (${property.type}) does not " +
                        "fit the value '$value' defined in ${owner.description}"
            )
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
            existing == null || existing.value == statement.value,
            "cannot add ${statement.description}, " +
                    "because ${owner.description} already defines a value '${existing?.value}'" +
                    "for property '${statement.propertyName}'"
        )
        map[statement.propertyName] = statement
    }

    fun addOrReplace(statement: PropertyValueStatement<Owner>) {
        if (get(statement.propertyName) != null) {
            map.remove(statement.propertyName)
        }
        add(statement)
    }

    fun get(propertyName: String): PropertyValueStatement<Owner>? {
        return map[propertyName]
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
                it.kind.fits(clazz) && clazz.definedPropertyValues.get(it.name) != null
            }
        }
        for (undefinedProperty in undefinedClassProperties) {
            results.checkKnown(
                get(undefinedProperty.name) != null,
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