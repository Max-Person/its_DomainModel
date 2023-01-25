package its.model

import its.model.dictionaries.*
import its.model.models.*

class DomainModel<
        C : ClassModel,
        V : DecisionTreeVarModel,
        E: EnumModel,
        P: PropertyModel,
        R: RelationshipModel>
    private constructor(
    private val classesDictionary: ClassesDictionaryBase<C>,
    private val decisionTreeVarsDictionary: DecisionTreeVarsDictionaryBase<V>,
    private val enumsDictionary: EnumsDictionaryBase<E>,
    private val propertiesDictionary: PropertiesDictionaryBase<P>,
    private val relationshipsDictionary: RelationshipsDictionaryBase<R>
        ) {

    companion object _static{
        private var instance : DomainModel<*, *, *, *, *>? = null

        @JvmStatic
        fun <C : ClassModel, V : DecisionTreeVarModel, E: EnumModel, P: PropertyModel, R: RelationshipModel>build(
            classesDictionary: ClassesDictionaryBase<C>,
            decisionTreeVarsDictionary: DecisionTreeVarsDictionaryBase<V>,
            enumsDictionary: EnumsDictionaryBase<E>,
            propertiesDictionary: PropertiesDictionaryBase<P>,
            relationshipsDictionary: RelationshipsDictionaryBase<R>
        ){
            instance = DomainModel(classesDictionary, decisionTreeVarsDictionary, enumsDictionary, propertiesDictionary, relationshipsDictionary)
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


    init{
        require(instance == null){
            "DomainModel является singleton-объектом. Создавать больше одного экзепляра такого объекта запрещено."
        }

        classesDictionary.validate()
        decisionTreeVarsDictionary.validate()
        enumsDictionary.validate()
        propertiesDictionary.validate()
        relationshipsDictionary.validate()

        instance = this
    }
}