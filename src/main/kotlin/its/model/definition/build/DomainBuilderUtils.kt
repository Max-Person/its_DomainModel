package its.model.definition.build

import its.model.definition.*
import its.model.definition.types.EnumValue

/**
 * Утилитарные функции для наполнения информацией домена [Domain]
 */
object DomainBuilderUtils {

    @JvmStatic
    fun Domain.newClass(className: String, parentName: String? = null): ClassDef {
        return classes.added(ClassDef(className, parentName))
    }

    @JvmStatic
    fun Domain.newObject(objectName: String, className: String): ObjectDef {
        return objects.added(ObjectDef(objectName, className))
    }

    @JvmStatic
    fun Domain.newVariable(varName: String, objectName: String): VariableDef {
        return variables.added(VariableDef(varName, objectName))
    }

    @JvmStatic
    fun <O : DomainDefWithMeta<O>> O.addMeta(locCode: String, metaPropertyName: String, value: Any) {
        metadata.add(MetadataProperty(metaPropertyName, locCode), value)
    }

    @JvmStatic
    fun <O : DomainDefWithMeta<O>> O.addMeta(metaPropertyName: String, value: Any) {
        metadata.add(MetadataProperty(metaPropertyName), value)
    }

    @JvmStatic
    fun <O : ClassInheritorDef<O>> O.setIntProperty(propertyName: String, value: Int) {
        definedPropertyValues.addOrReplace(PropertyValueStatement(this, propertyName, value))
    }

    @JvmStatic
    fun <O : ClassInheritorDef<O>> O.setEnumProperty(propertyName: String, enumName: String, enumValueName: String) {
        definedPropertyValues.addOrReplace(
            PropertyValueStatement(
                this,
                propertyName,
                EnumValue(enumName, enumValueName)
            )
        )
    }

    @JvmStatic
    fun <O : ClassInheritorDef<O>> O.setBoolProperty(propertyName: String, value: Boolean) {
        definedPropertyValues.addOrReplace(PropertyValueStatement(this, propertyName, value))
    }

    @JvmStatic
    fun ObjectDef.addRelationship(relationshipName: String, vararg objectNames: String) {
        relationshipLinks.add(RelationshipLinkStatement(this, relationshipName, objectNames.toList()))
    }
}