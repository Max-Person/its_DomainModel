package its.model.definition

import java.util.*

/**
 * Утверждение о связи объекта с другими объектами
 */
class RelationshipLinkStatement(
    override val owner: ObjectDef,
    val relationshipName: String,
    val objectNames: List<String>,
) : Statement<ObjectDef>() {
    override val description = "statement ${owner.name}=>$relationshipName(${objectNames.joinToString(", ")})"

    override fun copyForOwner(owner: ObjectDef) = RelationshipLinkStatement(owner, relationshipName, objectNames)

    internal fun getKnownObjects(results: DomainValidationResults): List<Optional<ObjectDef>> {
        return objectNames.map { objectName ->
            val objectOpt = domain.objects.get(objectName)
            results.checkKnown(
                objectOpt.isPresent,
                "No object definition '$objectName' found, while $description uses it as one of its objects"
            )
            objectOpt
        }
    }

    override fun validate(results: DomainValidationResults) {
        //известность отношения
        val relationshipOpt = owner.findRelationshipDef(relationshipName, results)
        if (relationshipOpt.isEmpty) {
            results.unknown(
                "No relationship definition '$relationshipName' found for $description"
            )
            return
        }
        val relationship = relationshipOpt.get()

        //Если зависимое, то стейтмент не нужен
        if (relationship.kind is DependantRelationshipKind) {
            results.invalid(
                "Statements are only allowed for base relationships, " +
                        "while ${relationship.description} is a dependant one (in $description)"
            )
            return
        }

        //совпадение объектов по количеству
        results.checkValid(
            objectNames.size == relationship.objectClassNames.size,
            "Invalid object count: ${relationship.objectClassNames.size} expected, but got ${objectNames.size} " +
                    "(in $description)"
        )

        //наличие объектов, совпадение по типу
        val foundObjects = getKnownObjects(results)
        foundObjects.forEachIndexed { i, objectOpt ->
            if (objectOpt.isPresent) {
                val obj = objectOpt.get()
                val classes = obj.getKnownInheritanceLineage(results)
                if (classes.isNotEmpty() && classes.last().parentName.isEmpty) { //Проверка выполняется, если иерархия завершена
                    val expectedClassName = relationship.objectClassNames[i]
                    results.checkValid(
                        classes.any { it.name == expectedClassName },
                        "${obj.description} in $description is expected to be an instance of ${expectedClassName}, but it is not"
                    )
                }
            }
        }
    }
}


class RelationshipLinkStatements(owner: ObjectDef) : Statements<ObjectDef, RelationshipLinkStatement>(owner) {
    private val list = mutableListOf<RelationshipLinkStatement>() //можно заменить на мультимапу для эффективности
    override val size: Int
        get() = list.size

    override fun isEmpty() = list.isEmpty()
    override fun containsAll(elements: Collection<RelationshipLinkStatement>) = list.containsAll(elements)
    override fun contains(element: RelationshipLinkStatement) = list.contains(element)
    override fun iterator() = list.iterator()


    override fun addToInner(statement: RelationshipLinkStatement) {
        list.add(statement)
    }

    fun listByName(relationshipName: String): List<RelationshipLinkStatement> {
        return this.filter { it.relationshipName == relationshipName }
    }
}