package its.model

import its.model.dictionaries.*
import its.model.models.*
import its.model.nodes.DecisionTreeNode
import its.model.nodes.StartNode

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
    @JvmField val relationshipsDictionary: RelationshipsDictionaryBase<R>
        ) {

    lateinit var decisionTree: StartNode

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
    }


    fun initFrom(directory : String){
        classesDictionary.fromCSV(directory + "classes.csv")
        decisionTreeVarsDictionary.fromCSV(directory + "vars.csv")
        enumsDictionary.fromCSV(directory + "enums.csv")
        propertiesDictionary.fromCSV(directory + "properties.csv")
        relationshipsDictionary.fromCSV(directory + "relationships.csv")

        classesDictionary.validate()
        decisionTreeVarsDictionary.validate()
        enumsDictionary.validate()
        propertiesDictionary.validate()
        relationshipsDictionary.validate()

        decisionTree = DecisionTreeNode.fromXMLFile(directory + "tree.xml")!!
    }
}