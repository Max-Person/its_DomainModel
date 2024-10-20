package its.model.nodes

import its.model.definition.DomainModel

/**
 * Общий класс для узлов агрегации результатов
 */
sealed class AggregationNode : LinkNode<BranchResult>(), EndingNode {
    abstract val aggregationMethod: AggregationMethod

    override fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext
    ) {
        results.checkValid(
            outcomes.keys.containsAll(aggregationMethod.necessaryContinuationOutcomes),
            "$description has to have continuation outcomes " +
                    "required by its type: ${aggregationMethod.necessaryContinuationOutcomes} "
        )
    }
}