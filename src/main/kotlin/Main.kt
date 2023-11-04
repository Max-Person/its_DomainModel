import its.model.DomainModel
import its.model.dictionaries.*





fun main(){
    val d = DomainModel(
        ClassesDictionary(),
        DecisionTreeVarsDictionary(),
        EnumsDictionary(),
        PropertiesDictionary(),
        RelationshipsDictionary(),
        "..\\inputs\\input_examples\\"
    )


    val v = CounterVisitor()
    v.process(d.decisionTree(""))

    println("There are ${v.count} tree nodes total")
}