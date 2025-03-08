import its.model.DomainSolvingModel


fun main(){
    val dir = "..\\inputs\\input_examples_expressions_prod"
    val model = DomainSolvingModel(
        dir,
        buildMethod = DomainSolvingModel.BuildMethod.LOQI
    ).validate()

    val v = CounterVisitor()
    v.process(model.decisionTree)

    println("There are ${v.count} tree nodes total")
}