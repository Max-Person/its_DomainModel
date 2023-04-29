package its.model

import its.model.dictionaries.*
import its.model.models.*
import its.model.nodes.DecisionTreeNode
import its.model.nodes.StartNode
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.RDFDataMgr

class DomainModel<
        C : ClassModel,
        V : DecisionTreeVarModel,
        E: EnumModel,
        P: PropertyModel,
        R: RelationshipModel>
    private constructor(
    @JvmField val classesDictionary: ClassesDictionaryBase<C>,
    @JvmField val decisionTreeVarsDictionary: DecisionTreeVarsDictionaryBase<V>,
    @JvmField val enumsDictionary: EnumsDictionaryBase<E>,
    @JvmField val propertiesDictionary: PropertiesDictionaryBase<P>,
    @JvmField val relationshipsDictionary: RelationshipsDictionaryBase<R>,
    @JvmField val domainRDF: Model = ModelFactory.createDefaultModel(),
        ) {

    @JvmField
    var decisionTree: StartNode? = null

    companion object _static{
        private var instance : DomainModel<*, *, *, *, *>? = null

        @JvmStatic
        fun <C : ClassModel, V : DecisionTreeVarModel, E: EnumModel, P: PropertyModel, R: RelationshipModel>collect(
            classesDictionary: ClassesDictionaryBase<C>,
            decisionTreeVarsDictionary: DecisionTreeVarsDictionaryBase<V>,
            enumsDictionary: EnumsDictionaryBase<E>,
            propertiesDictionary: PropertiesDictionaryBase<P>,
            relationshipsDictionary: RelationshipsDictionaryBase<R>
        ) : DomainModel<C, V, E, P, R>{
            require(instance == null){
                "DomainModel является singleton-объектом. Создавать больше одного экзепляра такого объекта запрещено."
            }
            instance = DomainModel(classesDictionary, decisionTreeVarsDictionary, enumsDictionary, propertiesDictionary, relationshipsDictionary)
            return instance as DomainModel<C, V, E, P, R>
        }

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
    }


    fun initFrom(directory : String){
        classesDictionary.fromCSV(directory + "classes.csv")
        decisionTreeVarsDictionary.fromCSV(directory + "vars.csv")
        enumsDictionary.fromCSV(directory + "enums.csv")
        propertiesDictionary.fromCSV(directory + "properties.csv")
        relationshipsDictionary.fromCSV(directory + "relationships.csv")
        domainRDF.read(RDFDataMgr.open(directory + "domain.ttl"), null, "TTL")

        classesDictionary.validate()
        decisionTreeVarsDictionary.validate()
        enumsDictionary.validate()
        propertiesDictionary.validate()
        relationshipsDictionary.validate()
        //TODO валидировать rdf модель на соответствие словарям

        decisionTree = DecisionTreeNode.fromXMLFile(directory + "tree.xml")!!
    }
}