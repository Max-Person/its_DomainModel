package its.model.definition.loqi

import its.model.definition.*
import its.model.definition.LinkQuantifier.Companion.ANY_COUNT
import its.model.definition.loqi.LoqiGrammarParser.*
import its.model.definition.loqi.LoqiStringUtils.extractEscapes
import its.model.definition.types.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import java.io.Reader
import java.util.concurrent.Callable

/**
 * Построение объекта [DomainModel] на основе языка LOQI
 */
class DomainLoqiBuilder private constructor(
    val domainModel: DomainModel = DomainModel(),
) : LoqiGrammarBaseVisitor<Any?>() {

    companion object {

        /**
         * Построить модель домена из LOQI.
         * При построении модель валидируется на валидность, **но не на полноту**
         *
         * TODO ошибки парсинга
         * TODO? больше информации про ошибки построения?
         * @param reader источник LOQI информации
         * @return построенная модель домена
         * @throws LoqiDomainBuildException в случае возникновении ошибок при построении модели
         */
        @JvmStatic
        fun buildDomain(reader: Reader): DomainModel {
            val lexer = LoqiGrammarLexer(CharStreams.fromReader(reader))
            val tokens = CommonTokenStream(lexer)
            val parser = LoqiGrammarParser(tokens)

            val errorListener = SyntaxErrorListener()
//            parser.errorListeners.clear()
            parser.addErrorListener(errorListener)

            val tree: ParseTree = parser.model()
            errorListener.getSyntaxErrors().firstOrNull()?.exception?.apply { throw this }

            val builder = DomainLoqiBuilder()
            tree.accept(builder)

            val domain = builder.domainModel
            builder.domainOpAt { domain.validateAndThrowInvalid() }
            return domain
        }
    }

    private fun <T> domainOpAt(line: Int = -1, expr: Callable<T>): T {
        val res: T
        try {
            res = expr.call()
        } catch (e: DomainDefinitionException) {
            if (line < 0) throw LoqiDomainBuildException(e.message ?: "", e)
            else throw LoqiDomainBuildException(line, e.message ?: "", e)
        }
        return res
    }

    override fun visitClassDecl(ctx: ClassDeclContext) {
        val line = ctx.id(0).start.line
        val name = ctx.id(0).getName()
        var parentName: String? = null

        if (ctx.id().size > 1) { //Есть второй id - значит указан класс-родитель
            parentName = ctx.id(1).getName()
        }

        val clazz = domainOpAt(line) { domainModel.classes.added(ClassDef(name, parentName)) }

        for (classMember in ctx.classMemberDecl()) {
            if (classMember.propertyDecl() != null)
                processPropertyDecl(clazz, classMember.propertyDecl())
            else if (classMember.relationshipDecl() != null)
                processRelationshipDecl(clazz, classMember.relationshipDecl())
            else if (classMember.propertyValueStatement() != null)
                processPropertyValueStatement(clazz, classMember.propertyValueStatement())
            else
                throw ThisShouldNotHappen()
        }

        clazz.fillMetadata(ctx.metadataSection())
    }

    private fun processPropertyDecl(clazz: ClassDef, ctx: PropertyDeclContext) {
        val line = ctx.id().start.line

        val kind = if (ctx.OBJ() != null) PropertyDef.PropertyKind.OBJECT else PropertyDef.PropertyKind.CLASS
        val name = ctx.id().getName()
        val type = if (ctx.type() != null) ctx.type().getType() else ctx.value().getTypeAndValue().type
        val value = ctx.value()?.getTypeAndValue()?.value
        val paramsDecl = getParamsDecl(ctx.paramsDecl())

        val property = domainOpAt(line) {
            clazz.declaredProperties.added(
                PropertyDef(clazz.name, name, type, kind, paramsDecl)
            )
        }
        if (value != null) {
            domainOpAt(ctx.value().start.line) {
                clazz.definedPropertyValues.add(
                    ClassPropertyValueStatement(clazz, name, ParamsValues.EMPTY, value)
                )
            }
        }
        property.fillMetadata(ctx.metadataSection())
    }

    private fun processRelationshipDecl(clazz: ClassDef, ctx: RelationshipDeclContext) {
        val line = ctx.id().start.line
        val name = ctx.id().getName()
        val objTypeNames = ctx.idList().id().map { it.getName() }
        val paramsDecl = getParamsDecl(ctx.paramsDecl())
        val kind = ctx.relationshipKind()?.getRelationshipKind(clazz.name, paramsDecl)

        if (paramsDecl.isNotEmpty() && kind is DependantRelationshipKind) {
            throw LoqiDomainBuildException(line, "Only base relationships can have params declaration")
        }

        val relationship = domainOpAt(line) {
            clazz.declaredRelationships.added(
                RelationshipDef(clazz.name, name, objTypeNames, kind ?: BaseRelationshipKind(paramsDecl = paramsDecl))
            )
        }
        relationship.fillMetadata(ctx.metadataSection())
    }

    private fun processPropertyValueStatement(clazz: ClassDef, ctx: PropertyValueStatementContext) {
        val line = ctx.value().start.line
        val name = ctx.id().getName()
        val paramsValues = getParamsValues(ctx.paramsValues())
        val value = ctx.value().getTypeAndValue().value

        domainOpAt(line) {
            clazz.definedPropertyValues.add(
                ClassPropertyValueStatement(
                    clazz,
                    name,
                    paramsValues,
                    value
                )
            )
        }
    }

    private fun getParamsDecl(ctx: ParamsDeclContext?): ParamsDecl {
        if (ctx == null) return ParamsDecl()
        return ParamsDecl(ctx.paramDecl().map { ParamDecl(it.id().getName(), it.type().getType()) })
    }

    private fun getParamsValues(ctx: ParamsValuesContext?): ParamsValues {
        if (ctx == null) return ParamsValues.EMPTY
        return when (ctx) {
            is NamedParamsValuesContext ->
                NamedParamsValues(ctx.namedParamValue().associate {
                    it.id().getName() to it.value().getTypeAndValue().value
                })

            is OrderedParamsValuesContext ->
                OrderedParamsValues(ctx.value().map { it.getTypeAndValue().value })

            else -> throw ThisShouldNotHappen()
        }
    }

    override fun visitEnumDecl(ctx: EnumDeclContext) {
        val line = ctx.id().start.line
        val name = ctx.id().getName()

        val enum = domainOpAt(line) { domainModel.enums.added(EnumDef(name)) }

        for (enumValueDecl in ctx.enumMemberList()?.enumMemberDecl() ?: emptyList()) {
            val tmpValue = EnumValueDef(enum.name, enumValueDecl.id().getName())
            val value = domainOpAt(enumValueDecl.start.line) { enum.values.added(tmpValue) }
            value.fillMetadata(enumValueDecl.metadataSection())
        }

        enum.fillMetadata(ctx.metadataSection())
    }

    override fun visitObjDecl(ctx: ObjDeclContext) {
        val line = ctx.id(0).start.line
        val name = ctx.id(0).getName()
        val className = ctx.id(1).getName()

        val obj = domainOpAt(line) { domainModel.objects.added(ObjectDef(name, className)) }

        for (objStatement in ctx.objStatement()) {
            if (objStatement.propertyValueStatement() != null)
                processPropertyValueStatement(obj, objStatement.propertyValueStatement())
            else if (objStatement.relationshipLinkStatement() != null)
                processRelationshipLinkStatement(obj, objStatement.relationshipLinkStatement())
            else
                throw ThisShouldNotHappen()
        }

        obj.fillMetadata(ctx.metadataSection())

        if (ctx.varLeftPart() != null) {
            for (varId in ctx.varLeftPart().idList().id()) {
                domainOpAt(varId.start.line) { domainModel.variables.add(VariableDef(varId.getName(), obj.name)) }
            }
        }
    }

    private fun processPropertyValueStatement(obj: ObjectDef, ctx: PropertyValueStatementContext) {
        val line = ctx.value().start.line
        val name = ctx.id().getName()
        val paramsValues = getParamsValues(ctx.paramsValues())
        val value = ctx.value().getTypeAndValue().value

        domainOpAt(line) { obj.definedPropertyValues.add(ObjectPropertyValueStatement(obj, name, paramsValues, value)) }
    }

    private fun processRelationshipLinkStatement(obj: ObjectDef, ctx: RelationshipLinkStatementContext) {
        val line = ctx.id().start.line
        val name = ctx.id().getName()
        val objNames = ctx.idList().id().map { it.getName() }
        val paramsValues = getParamsValues(ctx.paramsValues())

        domainOpAt(line) { obj.relationshipLinks.add(RelationshipLinkStatement(obj, name, objNames, paramsValues)) }
    }

    override fun visitVarDecl(ctx: VarDeclContext) {
        val valueObjectName = ctx.id().getName()
        for (id in ctx.varLeftPart().idList().id()) {
            domainOpAt(id.start.line) { domainModel.variables.add(VariableDef(id.getName(), valueObjectName)) }
        }
    }

    override fun visitAddMetaDecl(ctx: AddMetaDeclContext) {
        val ref = ctx.metaRef().getRef()
        val metadata = MetaData()
        metadata.fill(ctx.metadataSection())

        if (metadata.isEmpty()) return //Пустые метаданные игнорируем

        domainOpAt(ctx.metaRef().start.line) { domainModel.separateMetadata.add(ref, metadata) }
    }

    override fun visitAddClassDataDecl(ctx: AddClassDataDeclContext) {
        val ref = ClassRef(ctx.id().getName())
        val syntheticClass = syntheticClass(ref.className) //Костыль, потому что стейтменты не существует без владельца

        ctx.propertyValueStatement().forEach { propertyValue ->
            val name = propertyValue.id().getName()
            val paramsValues = getParamsValues(propertyValue.paramsValues())
            val value = propertyValue.value().getTypeAndValue().value
            domainOpAt(propertyValue.start.line) {
                syntheticClass.definedPropertyValues.add(
                    ClassPropertyValueStatement(syntheticClass, name, paramsValues, value)
                )
            }
        }

        if (syntheticClass.definedPropertyValues.isEmpty()) return //пустые стейтменты игнорируем

        domainOpAt(ctx.id().start.line) {
            domainModel.separateClassPropertyValues.add(ref, syntheticClass.definedPropertyValues)
        }
    }


    //---------Вспомогательные функции-----------------

    private fun IdContext.getName(): String {
        return ID().text.removeSurrounding("`")
    }

    private val SYNTHETIC = "LOQI_SYNTHETIC"
    private fun syntheticClass(name: String) = ClassDef(name, SYNTHETIC)

    private fun MetaRefContext.getRef(): DomainRef<*> {
        if (CLASS() != null) return ClassRef(id().getName())
        else if (ENUM() != null) return EnumRef(id().getName())
        else if (propertyRef() != null) return PropertyRef(propertyRef().id(0).getName(), propertyRef().id(1).getName())
        else if (relationshipRef() != null) return relationshipRef().getRef()
        else if (enumValueRef() != null) return enumValueRef().getRef()
        else return ObjectRef(id().getName())
    }

    private fun MetaData.fill(ctx: MetadataSectionContext?) {
        if (ctx == null) return
        for (metadataPropertyDecl in ctx.metadataPropertyDecl()) {
            val locCode =
                if (metadataPropertyDecl.id().size == 2)
                    metadataPropertyDecl.id(0).getName()
                else
                    null
            val propName = metadataPropertyDecl.id().last().getName()

            val value = metadataPropertyDecl.value().getTypeAndValue().value

            this.add(locCode, propName, value)
        }
    }

    private fun MetaOwner.fillMetadata(ctx: MetadataSectionContext?) {
        metadata.fill(ctx)
    }

    private fun TypeContext.getType(): Type<*> {
        if (intType() != null) return IntegerType(intType().intRange()?.getRange() ?: AnyNumber)
        if (doubleType() != null) return DoubleType(doubleType().doubleRange()?.getRange() ?: AnyNumber)
        if (BOOL_TYPE() != null) return BooleanType
        if (STRING_TYPE() != null) return StringType
        if (id() != null) return EnumType(id().getName()) //тип свойства, указанный как идентификатор, может быть только ссылкой на енам
        throw ThisShouldNotHappen()
    }

    private fun IntRangeContext.getRange(): Range {
        return if (intList() != null) DiscreteRange(intList().INTEGER().map { it.text.toDouble() }.toSet())
        else {
            val start =
                if (intRangeStart().INTEGER() != null) intRangeStart().INTEGER().text.toDouble()
                else Double.NEGATIVE_INFINITY
            val end =
                if (INTEGER() != null) INTEGER().text.toDouble()
                else Double.POSITIVE_INFINITY

            if (start.isInfinite() && end.isInfinite()) AnyNumber
            else ContinuousRange(start to end)
        }
    }

    private fun DoubleRangeContext.getRange(): Range {
        return if (doubleList() != null) DiscreteRange(doubleList().DOUBLE().map { it.text.toDouble() }.toSet())
        else {
            val start =
                if (doubleRangeStart().DOUBLE() != null) doubleRangeStart().DOUBLE().text.toDouble()
                else Double.NEGATIVE_INFINITY
            val end =
                if (DOUBLE() != null) DOUBLE().text.toDouble()
                else Double.POSITIVE_INFINITY

            if (start.isInfinite() && end.isInfinite()) AnyNumber
            else ContinuousRange(start to end)
        }
    }

    private fun ValueContext.getTypeAndValue(): TypeAndValue<*> {
        if (INTEGER() != null) return TypeAndValue(IntegerType(), INTEGER().text.toInt())
        if (DOUBLE() != null) return TypeAndValue(DoubleType(), DOUBLE().text.toDouble())
        if (BOOLEAN() != null) return TypeAndValue(BooleanType, BOOLEAN().text.toBoolean())
        if (STRING() != null) return TypeAndValue(StringType, STRING().text.extract())
        if (enumValueRef() != null) {
            val enumValue = enumValueRef().getRef()
            return TypeAndValue(EnumType(enumValue.enumName), enumValue)
        }
        throw ThisShouldNotHappen()
    }

    private fun String.extract(): String {
        var out = this
        if (out.startsWith("\"\"\"") || out.startsWith("'''")) {
            out = out.substring(3, out.length - 3)
            out = out.trimIndent()
        } else if (out.startsWith("\"") || out.startsWith("'")) {
            out = out.substring(1, out.length - 1)
        }
        return out.extractEscapes()
    }

    private fun RelationshipKindContext.getRelationshipKind(
        currentClassName: String,
        paramsDecl: ParamsDecl
    ): RelationshipKind {
        if (relationshipDependency() != null) {
            val type = getRelationshipDependencyType(relationshipDependency().relationshipDependencyType().text)
            val ref =
                if (relationshipDependency().id() != null)
                    RelationshipRef(currentClassName, relationshipDependency().id().getName())
                else
                    relationshipDependency().relationshipRef().getRef()
            return DependantRelationshipKind(type, ref)
        } else {
            val scale = scaleType()?.run { getRelationshipScale(this.text) }
            val quantifier = relationshipQuantifier()?.getQuantifier()
            return BaseRelationshipKind(scale, quantifier, paramsDecl)
        }
    }

    private fun EnumValueRefContext.getRef() = EnumValueRef(id(0).getName(), id(1).getName())

    private fun RelationshipRefContext.getRef() = RelationshipRef(id(0).getName(), id(1).getName())

    private fun getRelationshipDependencyType(string: String): DependantRelationshipKind.Type {
        return when (string) {
            "opposite" -> DependantRelationshipKind.Type.OPPOSITE
            "transitive" -> DependantRelationshipKind.Type.TRANSITIVE
            "between" -> DependantRelationshipKind.Type.BETWEEN
            "closer" -> DependantRelationshipKind.Type.CLOSER
            "further" -> DependantRelationshipKind.Type.FURTHER
            else -> throw ThisShouldNotHappen()
        }
    }

    private fun getRelationshipScale(string: String): BaseRelationshipKind.ScaleType {
        return BaseRelationshipKind.ScaleType.fromString(string) ?: throw ThisShouldNotHappen()
    }

    private fun RelationshipQuantifierContext.getQuantifier(): LinkQuantifier {
        return LinkQuantifier(
            linkCount(0).getLinkCount(),
            linkCount(1).getLinkCount()
        )
    }

    private fun LinkCountContext.getLinkCount() = if (INTEGER() != null) INTEGER().text.toInt() else ANY_COUNT
}