package its.model.definition.loqi

import its.model.definition.*
import its.model.definition.LinkQuantifier.Companion.ANY_COUNT
import its.model.definition.loqi.LoqiStringUtils.insertEscapes
import its.model.definition.loqi.LoqiStringUtils.toLoqiName
import its.model.definition.types.*
import java.io.Writer


/**
 * Опции при сохранении домена в LOQI
 */
enum class LoqiWriteOptions {
    /**
     * Выделить метаданные в отдельные секции
     */
    SEPARATE_METADATA,

    /**
     * Выделить значения свойств класса в отдельные секции
     */
    SEPARATE_CLASS_PROPERTY_VALUES,
}

/**
 * Сохранение домена ([DomainModel]) в LOQI формат
 */
class DomainLoqiWriter private constructor(
    private val domainModel: DomainModel,
    writer: Writer,
    private val saveOptions: Set<LoqiWriteOptions> = emptySet()
) {
    private val iWriter = IndentPrintWriter(writer)
    private fun indent() = iWriter.indent()
    private fun unindent() = iWriter.unindent()
    private fun write(s: Any) = iWriter.print(s)
    private fun writeln(s: Any) = iWriter.println(s)
    private fun newLine() = iWriter.println()
    private fun skipLines(n: Int = 2) = repeat(n) { newLine() }


    companion object {
        @JvmStatic
        fun saveDomain(domainModel: DomainModel, writer: Writer, saveOptions: Set<LoqiWriteOptions> = emptySet()) {
            DomainLoqiWriter(domainModel, writer, saveOptions).write()
        }
    }

    private fun write() {
        //секция классов
        writeClassSection()
        //Секция значений свойств классов
        writeClassPropertyValuesSection()
        //Секция статических метаданных
        writeSeparateStaticMetadataSection()
        //Свободные переменные
        writeVariableSection()
        //Секция объектов
        writeObjectSection()
        //Секция метаданных объектов
        writeSeparateObjectMetadataSection()

        iWriter.flush()
    }

    private fun writeClassSection() {
        if (domainModel.classes.isEmpty()) return
        writeCommentDelimiter("static (class) section")
        domainModel.enums.sortedBy { it.name }.forEach {
            it.writeEnum()
            skipLines()
        }
        domainModel.classes.sortedBy { (it.parentName ?: "!") + it.name }.forEach {
            it.writeClass()
            skipLines()
        }
    }

    private fun EnumDef.writeEnum() {
        write("enum ${name.toLoqiName()}")
        if (values.isNotEmpty()) {
            writeln(" {")
            indent()
            values.forEach {
                it.writeEnumValue()
                newLine()
            }
            unindent()
            write("}")
        }
        if (!hasOption(LoqiWriteOptions.SEPARATE_METADATA)) {
            metadata.writeMetadata()
        }
    }

    private fun EnumValueDef.writeEnumValue() {
        write(name.toLoqiName())
        if (!hasOption(LoqiWriteOptions.SEPARATE_METADATA)) {
            metadata.writeMetadata()
        }
        write(",")
    }

    private fun ClassDef.writeClass() {
        val hasBody = declaredProperties.isNotEmpty()
                || declaredRelationships.isNotEmpty()
                || (!hasOption(LoqiWriteOptions.SEPARATE_CLASS_PROPERTY_VALUES)
                && definedPropertyValues.isNotEmpty())
        write("$CLASS ${name.toLoqiName()}")
        parentName?.also { write(" : ${it.toLoqiName()}") }
        if (hasBody) {
            writeln(" {")
            indent()
            declaredProperties.forEach {
                it.writeProperty(this)
                newLine()
            }
            newLine()
            declaredRelationships.forEach {
                it.writeRelationship(this)
                newLine()
            }
            if (!hasOption(LoqiWriteOptions.SEPARATE_CLASS_PROPERTY_VALUES)) {
                val nonDeclaredPropertyValues = definedPropertyValues.filter {
                    !it.paramsValues.isEmpty() || declaredProperties.get(it.propertyName) == null
                }

                nonDeclaredPropertyValues.forEach {
                    it.writePropertyValue()
                    newLine()
                }
            }
            unindent()
            write("}")
        }
        if (!hasOption(LoqiWriteOptions.SEPARATE_METADATA)) {
            metadata.writeMetadata()
        }
    }

    private fun writeClassPropertyValuesSection() {
        if (!hasOption(LoqiWriteOptions.SEPARATE_CLASS_PROPERTY_VALUES)
            && domainModel.separateClassPropertyValues.isEmpty()
        ) return
        writeCommentDelimiter("separate static (class) property values section")
        if (hasOption(LoqiWriteOptions.SEPARATE_CLASS_PROPERTY_VALUES)) {
            domainModel.classes.forEach { writeSeparateClassPropertyValues(it.reference, it.definedPropertyValues) }
        }
        domainModel.separateClassPropertyValues.forEach { (ref, values) ->
            writeSeparateClassPropertyValues(ref, values)
        }
    }

    private fun writeSeparateClassPropertyValues(reference: ClassRef, values: Collection<PropertyValueStatement<*>>) {
        if (values.isEmpty()) return
        writeln("values for ${reference.toLoqiString()} {")
        indent()
        values.forEach {
            it.writePropertyValue()
            newLine()
        }
        unindent()
        write("}")
        skipLines()
    }

    private fun PropertyDef.writeProperty(owner: ClassDef) {
        val kindString = when (kind) {
            PropertyDef.PropertyKind.CLASS -> CLASS
            PropertyDef.PropertyKind.OBJECT -> OBJ
        }
        write("$kindString prop ${name.toLoqiName()}${paramsDecl.toLoqi()}: ${type.toLoqi()}")
        if (!hasOption(LoqiWriteOptions.SEPARATE_CLASS_PROPERTY_VALUES)
            && paramsDecl.isEmpty()
            && owner.definedPropertyValues.get(name) != null
        ) {
            val value = owner.definedPropertyValues.get(name)!!.value
            write(" = ${value.toLoqiValue()}")
        }
        if (!hasOption(LoqiWriteOptions.SEPARATE_METADATA)) {
            metadata.writeMetadata()
        }
        write(" ;")
    }

    private fun ParamsDecl.toLoqi(): String {
        if (this.isEmpty()) return ""
        return "<" + joinToString(", ") { param -> "${param.name.toLoqiName()} : ${param.type.toLoqi()}" } + ">"
    }

    private fun ParamsValues.toLoqi(): String {
        if (this.isEmpty()) return ""
        return when (this) {
            is OrderedParamsValues ->
                "<" + this.values.joinToString(", ") { it.toLoqiValue() } + ">"

            is NamedParamsValues ->
                "<" + this.valuesMap.entries.joinToString(", ") { (paramName, paramValue) -> "${paramName.toLoqiName()} = ${paramValue.toLoqiValue()}" } + ">"
        }
    }

    private fun PropertyValueStatement<*>.writePropertyValue() {
        write("${propertyName.toLoqiName()}${paramsValues.toLoqi()} = ${value.toLoqiValue()} ;")
    }

    private fun RelationshipDef.writeRelationship(owner: ClassDef) {
        val paramsString = if (kind is BaseRelationshipKind) effectiveParams.toLoqi() else ""
        write("rel ${name.toLoqiName()}$paramsString(${objectClassNames.map { it.toLoqiName() }.joinToString(", ")})")
        when (kind) {
            is BaseRelationshipKind -> if (isScalar || kind.quantifier != null) {
                write(" : ")
                if (isScalar) write(kind.scaleType!!.toLoqi())
                if (kind.quantifier != null) write(kind.quantifier.toLoqi())
            }

            is DependantRelationshipKind -> {
                val refString = if (kind.baseRelationshipRef.className == owner.name)
                    kind.baseRelationshipRef.relationshipName
                else
                    kind.baseRelationshipRef.toString()
                write(" : ${kind.type.toLoqi()} to $refString")
            }
        }
        if (!hasOption(LoqiWriteOptions.SEPARATE_METADATA)) {
            metadata.writeMetadata()
        }
        write(" ;")
    }

    private fun writeObjectSection() {
        if (domainModel.objects.isEmpty()) return
        writeCommentDelimiter("object section")
        domainModel.objects.sortedBy { it.className + it.name }.forEach {
            it.writeObject()
            skipLines()
        }
    }

    private fun ObjectDef.writeObject() {
        val variables = domainModel.variables.filter { it.valueObjectName == this.name }.map { it.name.toLoqiName() }
        if (variables.isNotEmpty()) writeln("var ${variables.joinToString(", ")} = ")

        write("$OBJ ${name.toLoqiName()} : ${className.toLoqiName()}")
        if (definedPropertyValues.isNotEmpty() || relationshipLinks.isNotEmpty()) {
            writeln(" {")
            indent()
            definedPropertyValues.forEach {
                it.writePropertyValue()
                newLine()
            }
            newLine()
            relationshipLinks.forEach {
                it.writeRelationshipLink()
                newLine()
            }
            unindent()
            write("}")
        }
        if (!hasOption(LoqiWriteOptions.SEPARATE_METADATA)) {
            metadata.writeMetadata()
        }
    }

    private fun RelationshipLinkStatement.writeRelationshipLink() {
        write(
            "${relationshipName.toLoqiName()}${paramsValues.toLoqi()}(${
                objectNames.map { it.toLoqiName() }.joinToString(", ")
            }) ;"
        )
    }

    private fun writeVariableSection() {
        val separateVariables = domainModel.variables.filter { domainModel.objects.get(it.valueObjectName) == null }
        if (separateVariables.isEmpty()) return

        writeCommentDelimiter("separate variables")
        val objectsToVars = separateVariables.groupBy { it.valueObjectName }
        objectsToVars.forEach { (obj, vars) ->
            writeln("var ${vars.map { it.name.toLoqiName() }.joinToString(", ")} = ${obj.toLoqiName()}")
        }
        newLine()
    }

    private fun writeSeparateStaticMetadataSection() {
        if (!hasOption(LoqiWriteOptions.SEPARATE_METADATA)
            && domainModel.separateMetadata.filterKeys { it !is ObjectRef }.isEmpty()
        ) return
        writeCommentDelimiter("separate static (class) metadata section")
        if (hasOption(LoqiWriteOptions.SEPARATE_METADATA)) {
            domainModel.enums.forEach { enum ->
                writeSeparateMetadata(enum.reference, enum.metadata)
                enum.values.forEach { writeSeparateMetadata(it.reference, it.metadata) }
            }
            domainModel.classes.forEach { clazz ->
                writeSeparateMetadata(clazz.reference, clazz.metadata)
                clazz.declaredProperties.forEach { writeSeparateMetadata(it.reference, it.metadata) }
                clazz.declaredRelationships.forEach { writeSeparateMetadata(it.reference, it.metadata) }
            }
        }
        newLine()
        domainModel.separateMetadata.filterKeys { it !is ObjectRef }.forEach { (ref, values) ->
            writeSeparateMetadata(ref, values)
        }
    }

    private fun writeSeparateObjectMetadataSection() {
        if ((!hasOption(LoqiWriteOptions.SEPARATE_METADATA)
                    || domainModel.objects.filter { it.metadata.isNotEmpty() }.isEmpty())
            && domainModel.separateMetadata.filterKeys { it is ObjectRef }.isEmpty()
        ) return

        writeCommentDelimiter("separate object metadata section")
        if (hasOption(LoqiWriteOptions.SEPARATE_METADATA)) {
            domainModel.objects.forEach { writeSeparateMetadata(it.reference, it.metadata) }
        }
        newLine()
        domainModel.separateMetadata.filterKeys { it is ObjectRef }.forEach { (ref, values) ->
            writeSeparateMetadata(ref, values)
        }
    }

    private fun writeSeparateMetadata(ref: DomainRef<*>, metadata: MetaData) {
        if (metadata.isEmpty()) return
        write("meta for ${ref.toLoqiString()}")
        metadata.writeMetadata()
        skipLines()
    }

    private fun MetaData.writeMetadata() {
        if (this.isEmpty()) return
        writeln(" [")
        indent()
        this.entries.forEach { (locCode, propertyName, value) ->
            locCode?.also { write("${it.toLoqiName()}.") }
            writeln("${propertyName.toLoqiName()} = ${value.toLoqiValue()} ;")
        }
        unindent()
        write("]")
    }

    //------ Вспомогательное ----

    private val CLASS = "class"
    private val OBJ = "obj"

    private fun hasOption(op: LoqiWriteOptions) = saveOptions.contains(op)

//    private fun <K, V> Map<K, V>.forEachBetween(
//        action: (Map.Entry<K, V>) -> Unit,
//        betweenAction: (Map.Entry<K, V>) -> Unit,
//    ) {
//        this.entries.forEachBetween(action, betweenAction)
//    }
//
//    private fun <T> Iterable<T>.forEachBetween(
//        action: (T) -> Unit,
//        betweenAction: (T) -> Unit,
//    ) {
//        val iterator = this.iterator()
//        while (iterator.hasNext()) {
//            val t = iterator.next()
//            action(t)
//            if (iterator.hasNext()) {
//                betweenAction(t)
//            }
//        }
//    }

    private fun DomainRef<*>.toLoqiString(): String {
        return when (this) {
            is ClassRef -> "class ${className.toLoqiName()}"
            is EnumRef -> "enum ${enumName.toLoqiName()}"
            is EnumValueRef -> "${enumName.toLoqiName()}:${valueName.toLoqiName()}"
            is ObjectRef -> "obj ${objectName.toLoqiName()}"
            is PropertyRef -> "${className.toLoqiName()}.${propertyName.toLoqiName()}"
            is RelationshipRef -> "${className.toLoqiName()}->${relationshipName.toLoqiName()}"
            is VariableRef -> throw ThisShouldNotHappen()
        }
    }

    private fun writeCommentDelimiter(title: String) {
        val len = 60 + title.length % 2
        val delim = "-".repeat(len)

        val centerIndex = len / 2
        val halfReplacementLength = title.length / 2

        val replaced = buildString {
            append(delim.substring(0, centerIndex - halfReplacementLength))
            append(title.uppercase())
            append(delim.substring(centerIndex + halfReplacementLength))
        }
        writeln("//$replaced")
        writeln("//$delim")
        newLine()
    }

    private fun Type<*>.toLoqi(): String {
        return when (this) {
            is BooleanType -> "bool"
            is EnumType -> enumName
            is DoubleType -> "double${range.toLoqi(false)}"
            is IntegerType -> "int${range.toLoqi(true)}"
            is StringType -> "string"
            else -> throw ThisShouldNotHappen()
        }
    }

    private fun Range.toLoqi(asInt: Boolean): String {
        return when (this) {
            AnyNumber -> ""
            is ContinuousRange -> "[${boundaries.first.toLoqiRange(asInt)},${boundaries.second.toLoqiRange(asInt)}]"
            is DiscreteRange -> "{${values.joinToString(", ") { it.toLoqiRange(asInt) }}}"
        }
    }

    private fun Double.toLoqiRange(asInt: Boolean): String {
        if (this.isInfinite()) return ""
        return if (asInt) this.toInt().toString() else this.toString()
    }

    //String, Integer, Double, Boolean, EnumValueRef
    private fun Any.toLoqiValue(): String {
        return when (this) {
            is String -> "\"${this.insertEscapes()}\""
            is EnumValueRef -> this.toLoqiString()
            else -> this.toString()
        }
    }

    private fun LinkQuantifier.toLoqi(): String {
        return "{${subjCount.toLoqiLinkCount()} -> ${objCount.toLoqiLinkCount()}}"
    }

    private fun Int.toLoqiLinkCount(): String {
        return if (this == ANY_COUNT) "*" else this.toString()
    }

    private fun BaseRelationshipKind.ScaleType.toLoqi(): String {
        return when (this) {
            BaseRelationshipKind.ScaleType.Linear -> "linear"
            BaseRelationshipKind.ScaleType.Partial -> "partial"
        }
    }

    private fun DependantRelationshipKind.Type.toLoqi(): String {
        return this.toString().lowercase() //FIXME стоит разделять стринговое представление и представление в локи
    }

}