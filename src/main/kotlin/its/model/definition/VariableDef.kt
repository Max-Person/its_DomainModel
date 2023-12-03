package its.model.definition

import java.util.*

class VariableDef(
    override val name: String,
    val valueObjectName: String,
) : DomainDef() {
    override val description = "variable $name"

    internal fun getKnownValueObject(results: DomainValidationResults): Optional<ObjectDef> {
        val objectOpt = domain.objects.get(valueObjectName)
        results.checkKnown(
            objectOpt.isPresent,
            "No object definition '$valueObjectName' found for variable $name"
        )
        return objectOpt
    }

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        //Существование объекта
        getKnownValueObject(results)
    }

    //---Операции (на валидном домене)---

    val valueObject: ObjectDef
        get() = getKnownValueObject(DomainValidationResultsThrowImmediately()).get()
}

class VariableContainer(domain: Domain) : RootDefContainer<VariableDef>(domain)