package its.model.definition

/**
 * Домен - модель предметной области
 */
class DomainModel : DomainElement() {
    override val domainModel = this
    override val description = "Domain"

    val classes = ClassContainer(this)
    val enums = EnumContainer(this)
    val objects = ObjectContainer(this)

    val variables = VariableContainer(this)

    val separateMetadata = SeparateMetadataContainer(this)
    val separateClassPropertyValues = SeparateClassPropertyValuesContainer(this)

    override fun validate(results: DomainValidationResults) {
        classes.validate(results)
        enums.validate(results)
        objects.validate(results)

        variables.validate(results)

        separateMetadata.validate(results)
        separateClassPropertyValues.validate(results)
    }

    /**
     * Создать глубокую копию этого домена
     */
    fun copy() = DomainModel().also { it.add(this) }

    /**
     * Добавить все определения из [other]
     * @see DefContainer.add
     */
    fun add(other: DomainModel) {
        classes.addAll(other.classes)
        enums.addAll(other.enums)
        objects.addAll(other.objects)

        variables.addAll(other.variables)

        separateMetadata.addAll(other.separateMetadata)
        separateClassPropertyValues.addAll(other.separateClassPropertyValues)
    }

    /**
     * Добавить все определения из [other] со слитием
     * @see DefContainer.addMerge
     */
    fun addMerge(other: DomainModel) {
        classes.addAllMerge(other.classes)
        enums.addAllMerge(other.enums)
        objects.addAllMerge(other.objects)

        variables.addAllMerge(other.variables)

        separateMetadata.addAll(other.separateMetadata)
        separateClassPropertyValues.addAll(other.separateClassPropertyValues)
    }

    fun subtract(other: DomainModel) {
        classes.subtract(other.classes)
        enums.subtract(other.enums)
        objects.subtract(other.objects)

        variables.subtract(other.variables)

        separateMetadata.subtract(other.separateMetadata)
        separateClassPropertyValues.subtract(other.separateClassPropertyValues)
    }
}