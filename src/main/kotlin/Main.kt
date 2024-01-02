import its.model.definition.compat.DictionariesRDFDomainBuilder
import its.model.nodes.xml.DecisionTreeXMLBuilder


fun main(){
    val dir = "..\\inputs\\input_examples\\"
    val domain = DictionariesRDFDomainBuilder.buildDomain(dir)

    val tree = DecisionTreeXMLBuilder.buildFromXMLFile("${dir}tree.xml")
    tree.validate(domain)

    val v = CounterVisitor()
    v.process(tree)

    println("There are ${v.count} tree nodes total")
}