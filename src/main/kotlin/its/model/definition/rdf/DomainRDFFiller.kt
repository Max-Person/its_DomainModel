package its.model.definition.rdf

import its.model.Utils.permutations
import its.model.definition.*
import its.model.definition.rdf.RDFUtils.POAS_PREF
import its.model.definition.rdf.RDFUtils.RDFS_PREF
import its.model.definition.rdf.RDFUtils.RDF_PREF
import its.model.definition.types.EnumType
import its.model.definition.types.EnumValue
import its.model.definition.types.Type
import org.apache.jena.rdf.model.*
import java.io.File
import java.util.*

/**
 * Заполнение домена данными из RDF модели [Model];
 *
 * Под заполнением понимается:
 * - для уже объявленных в домене классов:
 *      - присвоение их свойствам значений
 *      - заполнение метаданных (все неизвестные свойства считаются метаданными)
 *      - создание объектов - экземпляров класса
 * - для созданных и уже существовавших в домене объектов:
 *      - присвоение их свойствам значений
 *      - простановка между ними связей
 *      - заполнение метаданных (все неизвестные свойства считаются метаданными)
 *
 * **Построить** домен на основе RDF модели нельзя, т.к. RDF содержит только конкретные данные
 * (существование классов, объектов, значениях их свойств и связи между ними), но не содержит объявления -
 * декларативную информацию о типах и форматах ожидаемых данных. Поэтому RDF используется для **заполнения**
 */
class DomainRDFFiller protected constructor(
    val domain: Domain,
    val rdfModel: Model,
    val options: Set<Option> = emptySet(),
) {

    val basePrefix = rdfModel.getNsPrefixURI("") ?: POAS_PREF
    val rdfPrefix = rdfModel.getNsPrefixURI("rdf") ?: RDF_PREF
    val rdfsPrefix = rdfModel.getNsPrefixURI("rdfs") ?: RDFS_PREF

    enum class Option {
        THROW_INVALID_META,
        NARY_RELATIONSHIPS_OLD_COMPAT,
    }

    private fun hasOption(op: Option) = options.contains(op)

    companion object {
        /**
         * Заполнить домен [domain] данными из RDF-модели [rdfModel]
         * @param options параметры заполнения данных
         */
        @JvmStatic
        fun fillDomain(domain: Domain, rdfModel: Model, options: Set<Option> = emptySet()) {
            DomainRDFFiller(domain, rdfModel, options).fill()
            domain.validateAndThrowInvalid()
        }

        /**
         * @see fillDomain
         */
        @JvmStatic
        fun fillDomain(domain: Domain, turtleFilePath: String, options: Set<Option> = emptySet()) {
            fillDomain(
                domain,
                ModelFactory.createDefaultModel().read(
                    File(turtleFilePath).toURI().toURL().openStream().buffered(),
                    null,
                    "TTL"
                ),
                options,
            )
        }
    }

    private fun fill() {
        fillClasses()
        createAndFillObjects()
    }

    private fun fillClasses() {
        for (clazz in domain.classes) {
            val resourceOpt = findRdfResource(clazz.name)
            if (resourceOpt.isEmpty) continue
            val res = resourceOpt.get()
            fillClass(clazz, res)
        }
    }

    private fun fillClass(clazz: ClassDef, resource: Resource) {
        val usedRdfProperties = mutableSetOf<Property>()
        for (property in clazz.allProperties) {
            val rdfProperty = rdfModel.getProperty(basePrefix, property.name)!!
            val rdfStatement = resource.getProperty(rdfProperty) ?: continue
            val value = rdfStatement.`object`.asPropertyValue(property.type)
            clazz.definedPropertyValues.add(ClassPropertyValueStatement(clazz, property.name, value))
            usedRdfProperties.add(rdfProperty)
        }

        fillMeta(clazz, resource, usedRdfProperties)
    }

    private fun createAndFillObjects() {
        val objToResource = mutableSetOf<Pair<ObjectDef, Resource>>()
        for (objResource in findAllObjectResources()) {
            val obj = domain.objects.get(objResource.localName).orElseGet {
                val className = objResource.getProperty(typeRdfProp).`object`.asResource().localName
                domain.objects.add(ObjectDef(objResource.localName, className))
            }
            objToResource.add(obj to objResource)
        }

        //Объекты заполняются после создания, чтобы работала проверка типов в режиме совместимости (NARY_RELATIONSHIPS_OLD_COMPAT)
        objToResource.forEach { (obj, res) -> fillObject(obj, res) }
    }

    private fun fillObject(obj: ObjectDef, resource: Resource) {
        val usedRdfProperties = mutableSetOf<Property>()
        for (property in obj.clazz.allProperties) {
            val rdfProperty = rdfModel.getProperty(basePrefix, property.name)!!
            val rdfStatement = resource.getProperty(rdfProperty) ?: continue
            val value = rdfStatement.`object`.asPropertyValue(property.type)
            obj.definedPropertyValues.add(ObjectPropertyValueStatement(obj, property.name, value))
            usedRdfProperties.add(rdfProperty)
        }

        for (relationship in obj.clazz.allRelationships) {
            val rdfProperty = rdfModel.getProperty(basePrefix, relationship.name)!!
            for (rdfStatement in resource.listProperties(rdfProperty)) {
                val linkResource = rdfStatement.`object`.asResource()
                val linkObjNames = if (relationship.isBinary) {
                    listOf(linkResource.localName)
                } else if (relationship.isUnordered || hasOption(Option.NARY_RELATIONSHIPS_OLD_COMPAT)) {
                    linkResource.listProperties(rdfProperty).toList().map {
                        it.`object`.asResource().localName
                    }
                } else TODO("Разграничение на упорядоченные и неупорядоченные правила")

                if (!hasOption(Option.NARY_RELATIONSHIPS_OLD_COMPAT)) {
                    obj.relationshipLinks.add(RelationshipLinkStatement(obj, relationship.name, linkObjNames))
                } else {
                    //В старом RDF невозможно определить порядок объектов в связи
                    //Поэтому пытаемся перебрать все возможные способы проставить связь, чтобы она совпала по типам
                    val permutations = linkObjNames.permutations()
                    for ((i, linkObjectPerm) in permutations.withIndex()) {
                        try {
                            obj.relationshipLinks.add(RelationshipLinkStatement(obj, relationship.name, linkObjectPerm))
                            break
                        } catch (e: DomainDefinitionException) {
                            if (i == permutations.size - 1) throw e
                        }
                    }
                }
            }
            usedRdfProperties.add(rdfProperty)
        }

        val varStatement = resource.getProperty(varRdfProp)
        if (varStatement != null) {
            val varName = varStatement.`object`.asLiteral().string
            domain.variables.add(VariableDef(varName, obj.name))
            usedRdfProperties.add(varRdfProp)
        }

        fillMeta(obj, resource, usedRdfProperties)
    }


    private fun fillMeta(def: DomainDefWithMeta<*>, resource: Resource, nonMetaProperties: Set<Property>) {
        //Считаем метаданными все, кроме уже известных и использованных свойств
        val assumedMetaRdfStatements = resource.listProperties().filterKeep {
            val prop = it.predicate
            (prop.nameSpace == basePrefix || prop == labelRdfProp) && !nonMetaProperties.contains(prop)
        }.toList()

        for (rdfStatement in assumedMetaRdfStatements) {
            val obj = rdfStatement.`object`
            if (!obj.isLiteral) {
                if (hasOption(Option.THROW_INVALID_META)) {
                    //TODO возможно кидать такое же исключение, как в самих метаданных (когда и если оно будет)
                    throw IllegalArgumentException(
                        "rdf statement $rdfStatement was assumed to be metadata, but its object is not a literal"
                    )
                }
                continue
            }
            //Пытаемся распознать код локализации
            val metaName = rdfStatement.predicate.localName.replaceFirst("^([A-Z]{2})_(.+)".toRegex(), "$1.$2")
            def.metadata.add(
                MetadataProperty(metaName),
                obj.asLiteral().value
            )
        }
    }

    private fun RDFNode.asPropertyValue(expectedType: Type<*>): Any {
        //Используем expectedType только для подстановки енамов; Проверка типов будет сделана внутри самого домена
        if (this.isLiteral) {
            return this.asLiteral().value!!
        } else if (expectedType is EnumType) {
            return EnumValue(expectedType.enumName, this.asResource().localName)
        } else throw IllegalArgumentException(
            "Cannot extract property value of type $expectedType out of a Resource ($this) - should be literal"
        )
    }


    private val labelRdfProp
        get() = rdfModel.getProperty(rdfsPrefix, "label")

    private val varRdfProp
        get() = rdfModel.getProperty(basePrefix, "var...")

    private val typeRdfProp
        get() = rdfModel.getProperty(rdfPrefix, "type")

    private fun findAllObjectResources(): List<Resource> {
        //объектами считаются все ресурсы, являющиеся инстансом другого ресурса (т.е. субьекты свойства "тип")
        return rdfModel.listSubjectsWithProperty(typeRdfProp).filterKeep {
            val classResource = it.getProperty(typeRdfProp).`object`
            classResource.isResource && domain.classes.get(classResource.asResource().localName).isPresent
        }.toList()
    }

    private fun findRdfResource(name: String): Optional<Resource> {
        val res = rdfModel.getResource(basePrefix + name)!!
        return if (rdfModel.containsResource(res)) Optional.of(res) else Optional.empty<Resource>()
    }
}