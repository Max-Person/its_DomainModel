package its.model.definition

import java.util.*

class VariableDef(
    override val name: String,
    val valueObjectName: String,
) : DomainDef<VariableDef>() {
    override val description = "variable $name"
    override val reference = VariableRef(name)

    internal fun getKnownValueObject(results: DomainValidationResults): ObjectDef? {
        val obj = domainModel.objects.get(valueObjectName)
        results.checkKnown(
            obj != null,
            "No object definition '$valueObjectName' found for variable $name"
        )
        return obj
    }

    override fun validate(results: DomainValidationResults) {
        super.validate(results)

        //Существование объекта
        getKnownValueObject(results)
    }

    override fun plainCopy() = VariableDef(name, valueObjectName)

    //---Операции (на валидном домене)---

    val valueObject: ObjectDef
        get() = getKnownValueObject(DomainValidationResultsThrowImmediately())!!
}

class VariableContainer(domainModel: DomainModel) : RootDefContainer<VariableDef>(domainModel)

class VariableRef(
    val varName: String,
) : DomainRef<VariableDef> {
    override fun findIn(domainModel: DomainModel) = domainModel.variables.get(varName)
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