package its.model.definition.build

import its.model.definition.*
import its.model.definition.types.EnumValue

/**
 * Утилитарные функции для наполнения информацией домена [DomainModel]
 */
object DomainBuilderUtils {

    @JvmStatic
    fun DomainModel.newClass(className: String, parentName: String? = null): ClassDef {
        return classes.added(ClassDef(className, parentName))
    }

    @JvmStatic
    fun DomainModel.newObject(objectName: String, className: String): ObjectDef {
        return objects.added(ObjectDef(objectName, className))
    }

    @JvmStatic
    fun DomainModel.newVariable(varName: String, objectName: String): VariableDef {
        return variables.added(VariableDef(varName, objectName))
    }

    @JvmStatic
    fun <O : DomainDefWithMeta<O>> O.addMeta(locCode: String, metaPropertyName: String, value: Any) {
        metadata.add(locCode, metaPropertyName, value)
    }

    @JvmStatic
    fun <O : DomainDefWithMeta<O>> O.addMeta(metaPropertyName: String, value: Any) {
        metadata.add(metaPropertyName, value)
    }

    @JvmStatic
    fun <O : ClassInheritorDef<O>> O.setIntProperty(propertyName: String, value: Int) {
        definedPropertyValues.addOrReplace(PropertyValueStatement(this, propertyName, ParamsValues.EMPTY, value))
    }

    @JvmStatic
    fun <O : ClassInheritorDef<O>> O.setEnumProperty(propertyName: String, enumName: String, enumValueName: String) {
        definedPropertyValues.addOrReplace(
            PropertyValueStatement(
                this,
                propertyName,
                ParamsValues.EMPTY,
                EnumValue(enumName, enumValueName)
            )
        )
    }

    @JvmStatic
    fun <O : ClassInheritorDef<O>> O.setBoolProperty(propertyName: String, value: Boolean) {
        definedPropertyValues.addOrReplace(PropertyValueStatement(this, propertyName, ParamsValues.EMPTY, value))
    }

    @JvmStatic
    fun ObjectDef.addRelationship(relationshipName: String, vararg objectNames: String) {
        relationshipLinks.add(
            RelationshipLinkStatement(
                this,
                relationshipName,
                objectNames.toList(),
                ParamsValues.EMPTY
            )
        )
    }


    @JvmStatic
    fun splitMetadataPropertyName(propertyName: String, delimiter: Char = '_'): MetadataProperty {
        //TODO изменить эту систему разбивки (и например в XML можно использовать точку вместо _ )
        val splitRegex = "([A-Z]{2})\\$delimiter(.+)".toRegex()
        val splitMatchResult = splitRegex.matchEntire(propertyName)
        if (splitMatchResult != null) {
            val (locCode, property) = splitMatchResult.destructured
            return MetadataProperty(locCode, property)
        }
        return MetadataProperty(null, propertyName)
    }
}