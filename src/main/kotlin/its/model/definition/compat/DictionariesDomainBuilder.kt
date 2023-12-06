package its.model.definition.compat

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.definition.*
import its.model.definition.LinkQuantifier.Companion.ANY_COUNT
import its.model.definition.Utils.plus
import its.model.models.*
import java.io.File
import java.io.Reader
import java.net.URL
import java.util.*
import java.util.concurrent.Callable

/**
 * Построение объекта [Domain] на основе словарей (для обратной совместимости)
 *
 * Под словарем понимается CSV файл, столбцы которого разделены символами `'|'`.
 * Данные внутри ячеек словаря могут быть разделены символами `';'`
 */
class DictionariesDomainBuilder private constructor(
    val domain: Domain = Domain(),
    val enumsDictReader: Reader,
    val classesDictReader: Reader,
    val propertiesDictReader: Reader,
    val relationshipsDictReader: Reader,
) {

    companion object {
        /**
         * Посроить модель домена на основе словарей
         * @param enumsDictReader reader для словаря перечислений
         * @param classesDictReader reader для словаря классов
         * @param propertiesDictReader reader для словаря свойств
         * @param relationshipsDictReader reader для словаря отношений
         */
        @JvmStatic
        fun buildDomain(
            enumsDictReader: Reader,
            classesDictReader: Reader,
            propertiesDictReader: Reader,
            relationshipsDictReader: Reader,
        ): Domain {
            val builder = DictionariesDomainBuilder(
                Domain(),
                enumsDictReader,
                classesDictReader,
                propertiesDictReader,
                relationshipsDictReader
            )
            builder.build()
            val domain = builder.domain
            domain.validateAndThrowInvalid()
            return domain
        }

        /**
         * Посроить модель домена на основе словарей, взятых из директории [directoryUrl]
         *
         * Ожидается, что словари являются файломи в данной директории и называются
         * `'enums.csv'`, `'classes.csv'`, `'properties.csv'` и `'relationships.csv'` соответственно
         */
        @JvmStatic
        fun buildDomain(directoryUrl: URL) = buildDomain(
            (directoryUrl + "enums.csv").openStream().bufferedReader(),
            (directoryUrl + "classes.csv").openStream().bufferedReader(),
            (directoryUrl + "properties.csv").openStream().bufferedReader(),
            (directoryUrl + "relationships.csv").openStream().bufferedReader(),
        )

        /**
         * Посроить модель домена на основе словарей, взятых из директории [directoryPath]
         *
         * Ожидается, что словари являются файломи в данной директории и называются
         * `'enums.csv'`, `'classes.csv'`, `'properties.csv'` и `'relationships.csv'` соответственно
         */
        @JvmStatic
        fun buildDomain(directoryPath: String) = buildDomain(File(directoryPath).toURI().toURL())
    }

    private fun <T> domainOpAt(line: Int = -1, dict: String = "", expr: Callable<T>): T {
        val res: T
        try {
            res = expr.call()
        } catch (e: DomainDefinitionException) {
            if (line < 0 && dict == "") throw DictionariesDomainBuildException(e)
            else throw DictionariesDomainBuildException(line, dict, e)
        }
        return res
    }

    private fun checkCSVCorrect(requirement: Boolean, line: Int, dictionary: String, message: String) {
        if (!requirement) {
            throw DictionariesDomainBuildException(line, dictionary, message)
        }
    }

    private fun <T> assertedCsvOp(line: Int, dictionary: String, expr: Callable<T>): T {
        val res: T
        try {
            res = expr.call()
        } catch (e: IllegalArgumentException) {
            throw DictionariesDomainBuildException(line, dictionary, e.message ?: "")
        }
        return res
    }

    private fun build() {
        enumsDictReader.parseCSVandProcess(::readEnum)
        classesDictReader.parseCSVandProcess(::readClass)

        //Важно что свойства и отношения читаются после классов, т.к. приписываются к ним
        propertiesDictReader.parseCSVandProcess(::readProperty)
        relationshipsDictReader.parseCSVandProcess(::readRelationship)
    }

    private fun readEnum(rowNum: Int, csvRow: Array<String>) {
        val dictName = "Enums"
        checkCSVCorrect(
            csvRow.size >= 2,
            rowNum, dictName,
            "Enum dictionary must contain at least two columns: [enum name | list of enum value names]"
        )
        val enumName = csvRow[0]
        val enum = domain.enums.add(EnumDef(enumName))

        val enumValueNames = csvRow[1].split(LIST_ITEMS_SEPARATOR)
        enumValueNames.forEach { valueName -> enum.values.add(EnumValueDef(enumName, valueName)) }
    }

    private fun readClass(rowNum: Int, csvRow: Array<String>) {
        val dictName = "Classes"
        checkCSVCorrect(
            csvRow.size >= 2,
            rowNum, dictName,
            "Classes dictionary must contain at least two columns: [class name | parent class name]"
        )

        val className = csvRow[0]
        val parentName = if (csvRow[1].isNullOrBlank()) Optional.empty<String>() else Optional.of(csvRow[1])

        domainOpAt(rowNum, dictName) {
            domain.classes.add(ClassDef(className, parentName))
        }
    }

    private fun readProperty(rowNum: Int, csvRow: Array<String>) {
        val dictName = "Properties"
        checkCSVCorrect(
            csvRow.size >= 6,
            rowNum, dictName,
            "Properties dictionary must contain at least 6 columns: " +
                    "[property name | type | enum name (if needed) | " +
                    "is static | list of owner classes | value range (if needed)]"
        )

        val name = csvRow[0]
        val type = assertedCsvOp(rowNum, dictName) {
            getPropertyType(typeDecr = csvRow[1], enumName = csvRow[2], range = csvRow[5])
        }
        val kind = if (csvRow[3].toBoolean()) PropertyDef.PropertyKind.CLASS else PropertyDef.PropertyKind.OBJECT
        val declaringClassNames = csvRow[4].split(LIST_ITEMS_SEPARATOR)
        for (declaringClassName in declaringClassNames) {
            val declaringClass = domainOpAt(rowNum, dictName) {
                ClassRef(declaringClassName).findInOrUnkown(domain)
            } as ClassDef

            domainOpAt(rowNum, dictName) {
                declaringClass.declaredProperties.add(PropertyDef(declaringClassName, name, type, kind))
            }
        }
    }

    private fun readRelationship(rowNum: Int, csvRow: Array<String>) {
        val dictName = "Relationships"
        checkCSVCorrect(
            csvRow.size >= 6,
            rowNum, dictName,
            "Relationships dictionary must contain at least 6 columns: " +
                    "[relationship name | -skipped- | list of argument classes | " +
                    "scale type (if needed) | list of dependant relationship names (if scalar) | " +
                    "relation type (if needed) ]"
        )
        val name = csvRow[0]
        val argsClassNames = csvRow[2].split(LIST_ITEMS_SEPARATOR)
        checkCSVCorrect(
            argsClassNames.size >= 2,
            rowNum, dictName,
            "list of relationship's argument classes must be " +
                    "at least 2 long (subject class name, object class name); " +
                    "was ${argsClassNames.size} long"
        )

        val subjectClassName = argsClassNames[0]
        val objectClassNames = argsClassNames.subList(1, argsClassNames.size)
        val scaleType = assertedCsvOp(rowNum, dictName) { csvRow[3].getScaleType() }
        val quantifier = assertedCsvOp(rowNum, dictName) { csvRow[5].getLinkQuantifier() }
        val kind = BaseRelationshipKind(scaleType, quantifier)

        val subjectClass =
            domainOpAt(rowNum, dictName) { ClassRef(subjectClassName).findInOrUnkown(domain) } as ClassDef
        domainOpAt(rowNum, dictName) {
            subjectClass.declaredRelationships.add(RelationshipDef(subjectClassName, name, objectClassNames, kind))
        }
        if (scaleType.isPresent) {
            val dependantNames = csvRow[4].split(LIST_ITEMS_SEPARATOR)
            checkCSVCorrect(
                dependantNames.size == 6,
                rowNum, dictName,
                "list of a scalar relationship's dependant relationships must be at least 6 long " +
                        "(opposite, transitive, opposite-transitive, between, closer, further); " +
                        "was ${dependantNames.size} long"
            )

            val oppositeName = dependantNames[0]
            val oppositeKind = DependantRelationshipKind(
                DependantRelationshipKind.Type.OPPOSITE,
                RelationshipRef(subjectClassName, name) //ссылается на базовое отношение
            )
            domainOpAt(rowNum, dictName) {
                subjectClass.declaredRelationships.add(
                    RelationshipDef(
                        subjectClassName,
                        oppositeName,
                        listOf(subjectClassName),   //т.к. шкала задается между объектами одного типа
                        oppositeKind,
                    )
                )
            }

            val transitiveName = dependantNames[1]
            val transitiveKind = DependantRelationshipKind(
                DependantRelationshipKind.Type.TRANSITIVE,
                RelationshipRef(subjectClassName, name) //ссылается на базовое отношение
            )
            domainOpAt(rowNum, dictName) {
                subjectClass.declaredRelationships.add(
                    RelationshipDef(
                        subjectClassName,
                        transitiveName,
                        listOf(subjectClassName),   //т.к. шкала задается между объектами одного типа
                        transitiveKind,
                    )
                )
            }

            val oppositeTransitiveName = dependantNames[2]
            val oppositeTransitiveKind = DependantRelationshipKind(
                DependantRelationshipKind.Type.TRANSITIVE,
                RelationshipRef(subjectClassName, oppositeName) //ссылается на отношение, противоположное базовому
            )
            domainOpAt(rowNum, dictName) {
                subjectClass.declaredRelationships.add(
                    RelationshipDef(
                        subjectClassName,
                        oppositeTransitiveName,
                        listOf(subjectClassName),   //т.к. шкала задается между объектами одного типа
                        oppositeTransitiveKind,
                    )
                )
            }

            val betweenName = dependantNames[3]
            val betweenKind = DependantRelationshipKind(
                DependantRelationshipKind.Type.BETWEEN,
                RelationshipRef(subjectClassName, transitiveName) //ссылается на транзитивное отношение
            )
            domainOpAt(rowNum, dictName) {
                subjectClass.declaredRelationships.add(
                    RelationshipDef(
                        subjectClassName,
                        betweenName,
                        listOf(subjectClassName, subjectClassName),   //т.к. шкала задается между объектами одного типа
                        betweenKind,
                    )
                )
            }

            val closerName = dependantNames[4]
            val closerKind = DependantRelationshipKind(
                DependantRelationshipKind.Type.CLOSER,
                RelationshipRef(subjectClassName, transitiveName) //ссылается на транзитивное отношение
            )
            domainOpAt(rowNum, dictName) {
                subjectClass.declaredRelationships.add(
                    RelationshipDef(
                        subjectClassName,
                        closerName,
                        listOf(subjectClassName, subjectClassName),   //т.к. шкала задается между объектами одного типа
                        closerKind,
                    )
                )
            }

            val furtherName = dependantNames[5]
            val furtherKind = DependantRelationshipKind(
                DependantRelationshipKind.Type.FURTHER,
                RelationshipRef(subjectClassName, transitiveName) //ссылается на транзитивное отношение
            )
            domainOpAt(rowNum, dictName) {
                subjectClass.declaredRelationships.add(
                    RelationshipDef(
                        subjectClassName,
                        furtherName,
                        listOf(subjectClassName, subjectClassName),   //т.к. шкала задается между объектами одного типа
                        furtherKind,
                    )
                )
            }
        }

    }


    //---Вспомогательное-----


    /**
     * Разделитель элементов списка в ячейке CSV файла словаря
     */
    private val LIST_ITEMS_SEPARATOR = ';'

    /**
     * Разделитель столбцов в CSV файле словаря
     */
    private val COLUMNS_SEPARATOR = '|'
    private val csvParser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).withEscapeChar('\\').build()
    private fun Reader.parseCSVandProcess(rowProcessor: (rowNum: Int, row: Array<String>) -> Unit) {
        val csvReader = CSVReaderBuilder(this).withCSVParser(csvParser).build()
        csvReader.use { reader ->
            val rows = reader.readAll()
            rows.forEachIndexed { i, row -> rowProcessor(i + 1, row) }
        }
    }

    private fun getPropertyType(typeDecr: String, enumName: String? = null, range: String? = null): Type<*> {
        return when (typeDecr.uppercase()) {
            "STRING" -> StringType()
            "BOOL", "BOOLEAN" -> BooleanType()
            "INT", "INTEGER" -> IntegerType(range.toRange())
            "DOUBLE" -> DoubleType(range.toRange())
//            "COMPARISON", "COMPARISONRESULT", "COMPARISON_RESULT" -> EnumType(domain, "ComparisonResult") //FIXME
            "ENUM" -> EnumType(domain, enumName!!)
            else -> throw IllegalArgumentException("Cannot parse '$this' into a valid property type")
        }
    }

    /**
     * Разделитель диапазонов у свойств
     */
    private val RANGE_SEPARATOR = '-'
    private fun String?.toRange(): Range {
        if (this.isNullOrBlank())
            return AnyNumber

        if (this.contains(RANGE_SEPARATOR)) {
            val continuous = this.split(RANGE_SEPARATOR, ignoreCase = true, limit = 2)
            val boundaries = continuous[0].toDouble() to continuous[1].toDouble()
            return ContinuousRange(boundaries)
        } else {
            val discrete = this.split(LIST_ITEMS_SEPARATOR).map { it.toDouble() }
            return DiscreteRange(discrete)
        }
    }

    private fun String?.getScaleType(): Optional<RelationshipModel.ScaleType> {
        if (this.isNullOrBlank()) return Optional.empty()
        return when (this.lowercase()) {
            "linear" -> Optional.of(RelationshipModel.ScaleType.Linear)
            "partial" -> Optional.of(RelationshipModel.ScaleType.Partial)
            else -> throw IllegalArgumentException("Cannot parse '$this' into a valid relationship scale type")
        }
    }

    private fun String?.getLinkQuantifier(): Optional<LinkQuantifier> {
        if (this.isNullOrBlank()) return Optional.empty()
        return when (this.lowercase()) {
            "onetoone" -> Optional.of(LinkQuantifier(1, 1))
            "onetomany" -> Optional.of(LinkQuantifier(1, ANY_COUNT))
            else -> throw IllegalArgumentException("Cannot parse '$this' into a valid relationship link quantifier")
        }
    }
}