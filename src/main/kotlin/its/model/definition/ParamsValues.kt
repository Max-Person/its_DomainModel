package its.model.definition

/**
 * Значения параметров [ParamsDecl] для свойств и отношений.
 * Вспомогательный класс для утверждений [PropertyValueStatement] и [RelationshipLinkStatement]
 */
sealed class ParamsValues {
    /**
     * Представить значения в виде мапы "имя параметра - его значение"
     */
    abstract fun asMap(paramsDecl: ParamsDecl): Map<String, Any>

    /**
     * Заданы ли для параметров значения
     */
    abstract fun isEmpty(): Boolean

    /**
     * Валидация значений параметров с учетом их определения
     */
    internal open fun validate(
        paramsDecl: ParamsDecl,
        domainModel: DomainModel,
        mainStatement: Statement<*>,
        results: DomainValidationResults
    ) {
        val values = this.asMap(paramsDecl)
        results.checkValid(
            values.size <= paramsDecl.size,
            "Too many params are given for $mainStatement, expected ${paramsDecl.size}, but ${values.size} are present"
        )
        results.checkValid(
            values.size >= paramsDecl.size,
            "Too few params are given for $mainStatement, expected ${paramsDecl.size}, but ${values.size} are present"
        )
    }

    /**
     * Совпадают ли значения в текущем наборе с другим набором [other], основываясь на определении [paramsDecl].
     * Производится полное, строгое сравнение (равенство множеств значений)
     */
    fun matchesStrict(other: ParamsValues, paramsDecl: ParamsDecl): Boolean {
        return this.matchesStrict(other.asMap(paramsDecl), paramsDecl)
    }

    /**
     * Совпадают ли значения в текущем наборе с мапой значений [valuesMap], основываясь на определении [paramsDecl].
     * Производится полное, строгое сравнение (равенство множеств значений)
     */
    fun matchesStrict(valuesMap: Map<String, Any>, paramsDecl: ParamsDecl): Boolean {
        return this.asMap(paramsDecl) == valuesMap
    }

    /**
     * Содержит ли текущий набор значений все значения из мапы [valuesMap], основываясь на определении [paramsDecl]
     * Допускается частичное совпадение (текущее множество должно содержать переданное)
     */
    fun matchesPartial(valuesMap: Map<String, Any>, paramsDecl: ParamsDecl): Boolean {
        val asMap = this.asMap(paramsDecl)
        return valuesMap.all { (paramName, value) -> asMap[paramName] == value }
    }

    companion object {
        /**
         * Пустой набор значений параметров
         */
        @JvmStatic
        val EMPTY: ParamsValues
            get() = NamedParamsValues(mapOf())
    }
}

/**
 * Упорядоченный набор значений параметров.
 * Использует порядок, в котором указаны значения, для определения того, какому из параметров они соответствуют
 * - так можно не указывать названия самих параметров.
 */
class OrderedParamsValues(
    val values: List<Any>,
) : ParamsValues() {
    override fun asMap(paramsDecl: ParamsDecl): Map<String, Any> {
        return values.mapIndexed { index, value ->
            paramsDecl[index].name to value
        }.toMap()
    }

    override fun isEmpty() = values.isEmpty()

    override fun toString(): String {
        return "<${values.joinToString(separator = ", ")}>"
    }

    override fun validate(
        paramsDecl: ParamsDecl,
        domainModel: DomainModel,
        mainStatement: Statement<*>,
        results: DomainValidationResults
    ) {
        super.validate(paramsDecl, domainModel, mainStatement, results)
        values.forEachIndexed { index, value ->
            val param = paramsDecl[index]
            results.checkValid(
                param.type.fits(value, domainModel),
                "Value '$value' at pos $index in parameters of $mainStatement" +
                        " does not match the expected type of parameter '${param.name}' (${param.type})"
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderedParamsValues

        return values == other.values
    }

    override fun hashCode(): Int {
        return values.hashCode()
    }
}

/**
 * Именованный набор значений параметров.
 * Использует явно заданные названия параметров для определения того, какому из параметров соответствует какое значение
 * - это понятнее читать и позволяет указывать значения в произвольном порядке.
 */
class NamedParamsValues(
    val valuesMap: Map<String, Any>,
) : ParamsValues() {
    override fun asMap(paramsDecl: ParamsDecl): Map<String, Any> {
        return valuesMap
    }

    override fun isEmpty() = valuesMap.isEmpty()

    override fun toString(): String {
        return "<${valuesMap.map { (paramName, value) -> "$paramName = $value" }.joinToString(", ")}>"
    }

    override fun validate(
        paramsDecl: ParamsDecl,
        domainModel: DomainModel,
        mainStatement: Statement<*>,
        results: DomainValidationResults
    ) {
        super.validate(paramsDecl, domainModel, mainStatement, results)
        valuesMap.forEach { (paramName, value) ->
            val param = paramsDecl[paramName]
            if (param != null) {
                results.checkValid(
                    param.type.fits(value, domainModel),
                    "Value '$value' does not match the expected type " +
                            "of parameter '${param.name}' (${param.type}) in $mainStatement"
                )
            } else {
                results.invalid(
                    "No parameter with name '$paramName' exists to use in $mainStatement"
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NamedParamsValues

        return valuesMap == other.valuesMap
    }

    override fun hashCode(): Int {
        return valuesMap.hashCode()
    }


}
