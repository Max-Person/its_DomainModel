package its.model.definition.rdf

import its.model.definition.*
import its.model.definition.rdf.DomainRDFWriter.Option
import its.model.definition.rdf.RDFUtils.XSD_PREF
import its.model.definition.types.EnumValue
import org.apache.jena.datatypes.xsd.XSDDatatype
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import java.io.Writer

/**
 * Записать данные из домена [Domain] в RDF
 *
 * Данный формат записи совместим с [DomainRDFFiller] (с учетом [Option.NARY_RELATIONSHIPS_OLD_COMPAT] в обоих классах),
 * и соответственно записывает в рдф только информацию о классах, объектах и их содержимом.
 * Полноценно восстановить домен **только** из RDF не получится за нехваткой данных
 */
class DomainRDFWriter private constructor(
    val domain: Domain,
    val basePrefix: String = RDFUtils.POAS_PREF,
    val options: Set<Option> = emptySet(),
) {
    private val rdfModel: Model = ModelFactory.createDefaultModel()

    private val rdfPrefix = RDFUtils.RDF_PREF
    private val rdfsPrefix = RDFUtils.RDFS_PREF

    enum class Option {
        NARY_RELATIONSHIPS_OLD_COMPAT,
    }

    private fun hasOption(op: Option) = options.contains(op)

    companion object {
        @JvmStatic
        fun saveDomain(
            domain: Domain,
            basePrefix: String = RDFUtils.POAS_PREF,
            saveOptions: Set<Option> = emptySet()
        ): Model {
            return DomainRDFWriter(domain, basePrefix, saveOptions).write()
        }

        @JvmStatic
        fun saveDomain(
            domain: Domain,
            writer: Writer,
            basePrefix: String = RDFUtils.POAS_PREF,
            saveOptions: Set<Option> = emptySet()
        ) {
            saveDomain(domain, basePrefix, saveOptions)
                .write(writer, "TTL", basePrefix)
        }
    }

    private fun write(): Model {
        rdfModel.setNsPrefix("", basePrefix)
        rdfModel.setNsPrefix("rdf", rdfPrefix)
        rdfModel.setNsPrefix("rdfs", rdfsPrefix)
        rdfModel.setNsPrefix("xsd", XSD_PREF)

        domain.classes.forEach { it.writeClass() }
        domain.objects.forEach { it.writeObject() }
        domain.variables.forEach { it.writeVariable() }

        return rdfModel
    }

    private fun VariableDef.writeVariable() {
        val varRdfProp = rdfModel.getProperty(basePrefix, "var...")
        findResource(valueObjectName).addProperty(varRdfProp, name, XSDDatatype.XSDstring)
    }

    private fun ClassDef.writeClass() {
        val resource = createResource(name)
        if (parentName != null) {
            val subclassRdfProp = rdfModel.getProperty(rdfsPrefix, "subClassOf")
            val parentResource = findResource(parentName)
            resource.addProperty(subclassRdfProp, parentResource)
        }
        definedPropertyValues.forEach { it.writeProperty(resource) }
        writeMetadata(resource)
    }

    private fun ObjectDef.writeObject() {
        val resource = createResource(name)

        val typeRdfProp = rdfModel.getProperty(rdfPrefix, "type")
        val classResource = findResource(className)
        resource.addProperty(typeRdfProp, classResource)

        definedPropertyValues.forEach { it.writeProperty(resource) }
        relationshipLinks.forEach { it.writeLink(resource) }
        writeMetadata(resource)
    }

    private fun MetaOwner.writeMetadata(ownerResource: Resource) {
        metadata.entries.forEach { (locCode, propertyName, value) ->
            val metaName = (locCode?.plus("_") ?: "") + propertyName
            return writeProperty(ownerResource, metaName, value)
        }
    }

    private fun PropertyValueStatement<*>.writeProperty(ownerResource: Resource) {
        return writeProperty(ownerResource, propertyName, value)
    }
    private fun writeProperty(ownerResource: Resource, propertyName: String, value: Any) {
        val rdfProp = rdfModel.getProperty(basePrefix, propertyName)
        when (value) {
            is Boolean, is String, is Int, is Double -> ownerResource.addLiteral(rdfProp, value)
            is EnumValue -> ownerResource.addProperty(rdfProp, findResource((value as EnumValue).valueName))
            else -> throw ThisShouldNotHappen()
        }
    }

    private val relationshipLinkCounts = mutableMapOf<String, Int>()
    private fun getUniqueLinkResource(relationshipName: String): Resource {
        val count = relationshipLinkCounts[relationshipName] ?: 0
        val res = findResource("${relationshipName}_link_$count")
        relationshipLinkCounts[relationshipName] = count + 1
        return res
    }

    private fun RelationshipLinkStatement.writeLink(ownerResource: Resource) {
        val relationship = this.relationship
        val rdfProp = rdfModel.getProperty(basePrefix, relationshipName)
        if (this.relationship.isBinary) {
            val objRes = findResource(objectNames.first())
            ownerResource.addProperty(rdfProp, objRes)
        } else {
            val linkRes = getUniqueLinkResource(relationshipName)
            val subjRdfProp = if (hasOption(Option.NARY_RELATIONSHIPS_OLD_COMPAT)) rdfProp
            else rdfModel.getProperty(basePrefix, "${relationshipName}_subj")
            ownerResource.addProperty(subjRdfProp, linkRes)
            if (this.relationship.isUnordered) {
                val objRdfProp = if (hasOption(Option.NARY_RELATIONSHIPS_OLD_COMPAT)) rdfProp
                else rdfModel.getProperty(basePrefix, "${relationshipName}_obj")
                objectNames.forEach { objName ->
                    val objRes = findResource(objName)
                    linkRes.addProperty(objRdfProp, objRes)
                }
            } else {
                objectNames.forEachIndexed { i, objName ->
                    val objRdfProp = if (hasOption(Option.NARY_RELATIONSHIPS_OLD_COMPAT)) rdfProp
                    else rdfModel.getProperty(basePrefix, "${relationshipName}_obj_$i")
                    val objRes = findResource(objName)
                    linkRes.addProperty(objRdfProp, objRes)
                }
            }
        }
    }

    private val usedNames = mutableSetOf<String>()
    private fun createResource(name: String): Resource {
        if (usedNames.contains(name)) {
            throw IllegalArgumentException(
                "It's assumed that in RDF names for all definitions are unique, but $name was used multiple times"
            )
        }
        return findResource(name)
    }

    private fun findResource(name: String): Resource {
        return rdfModel.getResource(basePrefix + name)
    }
}