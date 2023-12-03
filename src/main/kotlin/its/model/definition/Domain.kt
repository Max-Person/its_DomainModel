package its.model.definition

/**
 * Домен - модель предметной области
 */
class Domain : DomainElement() {
    override val domain = this
    override val description = "Domain"

    val classes = ClassContainer(this)
    val enums = EnumContainer(this)
    val objects = ObjectContainer(this)

    val variables = VariableContainer(this)

    internal val separateMetadata = SeparateMetadataContainer(this)
    internal val separateClassPropertyValues = SeparateClassPropertyValuesContainer(this)

    override fun validate(results: DomainValidationResults) {
        classes.validate(results)
        enums.validate(results)
        objects.validate(results)

        //TODO? валидировать квантификаторы отношений?

        variables.validate(results)

        separateMetadata.validate(results)
        separateClassPropertyValues.validate(results)
    }
}