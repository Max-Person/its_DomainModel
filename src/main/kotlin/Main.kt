import its.model.DomainSolvingModel


fun main(){
    val dir = "..\\inputs\\input_examples"
    val model = DomainSolvingModel(dir).validate()

    val v = CounterVisitor()
    v.process(model.decisionTree)

    println("There are ${v.count} tree nodes total")
}