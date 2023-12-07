package its.model.definition

import java.util.*

class VariableDef(
    override val name: String,
    val valueObjectName: String,
) : DomainDef() {
    override val description = "variable $name"
    override val reference = VariableRef(name)

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

    override fun plainCopy() = VariableDef(name, valueObjectName)

    //---Операции (на валидном домене)---

    val valueObject: ObjectDef
        get() = getKnownValueObject(DomainValidationResultsThrowImmediately()).get()
}

class VariableContainer(domain: Domain) : RootDefContainer<VariableDef>(domain)

class VariableRef(
    val varName: String,
) : DomainRef {
    override fun findIn(domain: Domain) = domain.variables.get(varName) as Optional<DomainDefWithMeta>
    override fun toString() = "variable $varName"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VariableRef) return false

        if (varName != other.varName) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(this::class, varName)
    }
}