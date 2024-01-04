package its.model.definition

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

    internal fun getKnownRelationship(results: DomainValidationResults): RelationshipDef? {
        val relationship = owner.findRelationshipDef(relationshipName, results)
        results.checkKnown(
            relationship != null,
            "No relationship definition '$relationshipName' found for $description"
        )
        return relationship
    }

    internal fun getKnownObjects(results: DomainValidationResults): List<ObjectDef?> {
        return objectNames.map { objectName ->
            val obj = domain.objects.get(objectName)
            results.checkKnown(
                obj != null,
                "No object definition '$objectName' found, while $description uses it as one of its objects"
            )
            obj
        }
    }

    override fun validate(results: DomainValidationResults) {
        //известность отношения
        val relationship = getKnownRelationship(results) ?: return

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
        foundObjects.forEachIndexed { i, obj ->
            if (obj != null) {
                val classes = obj.getKnownInheritanceLineage(results)
                if (classes.isNotEmpty() && classes.last().parentName == null) { //Проверка выполняется, если иерархия завершена
                    val expectedClassName = relationship.objectClassNames[i]
                    results.checkValid(
                        classes.any { it.name == expectedClassName },
                        "${obj.description} in $description is expected to be an instance of ${expectedClassName}, but it is not"
                    )
                }
            }
        }
    }

    val relationship: RelationshipDef
        get() = getKnownRelationship(DomainValidationResultsThrowImmediately())!!

    val objects: List<ObjectDef>
        get() = getKnownObjects(DomainValidationResultsThrowImmediately()).requireNoNulls()
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