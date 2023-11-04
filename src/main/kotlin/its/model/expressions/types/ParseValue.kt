package its.model.expressions.types

import its.model.DomainModel
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

object ParseValue : Types.TypeBehaviour<String, Any>() {
    fun String.parseValue(type: KClass<*>): Any {
        return type.exec(this)
    }

    fun String.parseValueForBranchResult(): Any { //FIXME нужно правильное определение типов
        return Types.Boolean.exec(this)
    }

    override fun forString(param: String): String {
        return param
    }

    override fun forBoolean(param: String): Boolean {
        return param.toBoolean()
    }

    override fun forInt(param: String): Int {
        return param.toInt()
    }

    override fun forDouble(param: String): Double {
        return param.toDouble()
    }

    override fun forEnum(param: String): Any {
        val split = param.split(':', limit = 2)
        require(split.size == 2) { "'$param' должно иметь форму 'enum:value' для возможности парсинга в enum" }
        val ownerEnum = split[0]
        val value = split[1]
        require(DomainModel.enumsDictionary.exist(ownerEnum)) { "Enum ${ownerEnum} не объявлен в словаре." }
        require(DomainModel.enumsDictionary.containsValue(ownerEnum, value) == true) {
            "Enum ${ownerEnum} не содержит значения ${value}."
        }
        return EnumValue(ownerEnum, value)
    }

    override fun forComparisonResult(param: String): ComparisonResult {
        return ComparisonResult.fromString(param)
            ?: throw IllegalArgumentException("Cannot transform '$param' into ComparisonResult")
    }

    override fun forClazz(param: String): Clazz {
        require(DomainModel.classesDictionary.exist(param)) { "Класс $param не объявлен в словаре." }
        return DomainModel.classesDictionary.get(param)!!
    }

    override fun forObj(param: String): Any {
        throw IllegalArgumentException("Parsing strings into Obj is forbidden")
    }

}