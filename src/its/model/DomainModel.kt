package its.model

import its.model.dictionaries.*
import its.model.models.*
import its.model.nodes.DecisionTreeNode
import its.model.nodes.StartNode
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import java.io.File
import java.net.URL

open class DomainModel<
        C : ClassModel,
        V : DecisionTreeVarModel,
        E: EnumModel,
        P: PropertyModel,
        R: RelationshipModel>
    (
        @JvmField val classesDictionary: ClassesDictionaryBase<C>,
        @JvmField val decisionTreeVarsDictionary: DecisionTreeVarsDictionaryBase<V>,
        @JvmField val enumsDictionary: EnumsDictionaryBase<E>,
        @JvmField val propertiesDictionary: PropertiesDictionaryBase<P>,
        @JvmField val relationshipsDictionary: RelationshipsDictionaryBase<R>,
        directoryUrl: URL,
    ) {

    constructor(
        classesDictionary: ClassesDictionaryBase<C>,
        decisionTreeVarsDictionary: DecisionTreeVarsDictionaryBase<V>,
        enumsDictionary: EnumsDictionaryBase<E>,
        propertiesDictionary: PropertiesDictionaryBase<P>,
        relationshipsDictionary: RelationshipsDictionaryBase<R>,
        directoryPath: String
    ) : this(classesDictionary, decisionTreeVarsDictionary, enumsDictionary, propertiesDictionary, relationshipsDictionary, File(directoryPath).toURI().toURL())

    @JvmField
    val domainRDF: Model = ModelFactory.createDefaultModel()

    @JvmField
    var decisionTree: StartNode? = null

    init {
        require(instance == null){
            "DomainModel является singleton-объектом. Создавать больше одного экзепляра такого объекта запрещено."
        }

        instance = this

        classesDictionary.fromCSV((directoryUrl + "classes.csv").openStream().bufferedReader())
        decisionTreeVarsDictionary.fromCSV((directoryUrl + "vars.csv").openStream().bufferedReader())
        enumsDictionary.fromCSV((directoryUrl + "enums.csv").openStream().bufferedReader())
        propertiesDictionary.fromCSV((directoryUrl + "properties.csv").openStream().bufferedReader())
        relationshipsDictionary.fromCSV((directoryUrl + "relationships.csv").openStream().bufferedReader())
        domainRDF.read((directoryUrl + "domain.ttl").openStream().buffered(), null, "TTL")

        classesDictionary.validate()
        decisionTreeVarsDictionary.validate()
        enumsDictionary.validate()
        propertiesDictionary.validate()
        relationshipsDictionary.validate()
        //TODO валидировать rdf модель на соответствие словарям

        decisionTree = DecisionTreeNode.fromXMLFile((directoryUrl  + "tree.xml").toURI().toString())!!
    }

    companion object _static{
        @JvmStatic
        protected var instance : DomainModel<*, *, *, *, *>? = null

        @JvmStatic
        val classesDictionary
            get() = instance!!.classesDictionary

        @JvmStatic
        val decisionTreeVarsDictionary
            get() = instance!!.decisionTreeVarsDictionary

        @JvmStatic
        val enumsDictionary
            get() = instance!!.enumsDictionary

        @JvmStatic
        val propertiesDictionary
            get() = instance!!.propertiesDictionary

        @JvmStatic
        val relationshipsDictionary
            get() = instance!!.relationshipsDictionary

        @JvmStatic
        val decisionTree
            get() = instance!!.decisionTree!!

        @JvmStatic
        val domainRDF
            get() = instance!!.domainRDF

        @JvmStatic
        protected operator fun URL.plus(s : String) : URL {
            return URL(
                this.protocol,
                this.host,
                this.port,
                this.path + (if(this.path.endsWith("/")) "" else "/") + s + (if(this.query.isNullOrBlank()) "" else "?" + this.query),
                null
            )
        }
    }
}