package its.model.dictionaries

import com.github.drapostolos.typeparser.NullStringStrategyHelper
import com.github.drapostolos.typeparser.SplitStrategyHelper
import com.github.drapostolos.typeparser.TypeParser
import its.model.models.RelationshipModel

class RelationshipsDictionary(path: String) : RelationshipsDictionaryBase<RelationshipModel>(path, RelationshipModel::class)

object DictionariesUtil {

    /**
     * Разделитель столбцов в CSV файле словаря
     */
    const val COLUMNS_SEPARATOR = '|'

    /**
     * Разделитель элементов списка в ячейке CSV файла словаря
     */
    const val LIST_ITEMS_SEPARATOR = ';'

    @JvmField
    val valueParser = TypeParser.newBuilder().setSplitStrategy { s: String, h: SplitStrategyHelper ->
        s.split(LIST_ITEMS_SEPARATOR)
    }.setNullStringStrategy{ s: String, nullStringStrategyHelper: NullStringStrategyHelper -> s.equals("")}.build()

    /**
     * Инициализирует все словари и проверят их валидность
     */
    fun collectDictionaries(
        classesDictionaryPath: String,
        decisionTreeVarsDictionaryPath: String,
        enumsDictionaryPath: String,
        propertiesDictionaryPath: String,
        relationshipsDictionary: RelationshipsDictionary
    ) {
        ClassesDictionary.init(classesDictionaryPath)
        DecisionTreeVarsDictionary.init(decisionTreeVarsDictionaryPath)
        EnumsDictionary.init(enumsDictionaryPath)
        PropertiesDictionary.init(propertiesDictionaryPath)
        //RelationshipsDictionary.init(relationshipsDictionaryPath)

        ClassesDictionary.validate()
        DecisionTreeVarsDictionary.validate()
        EnumsDictionary.validate()
        PropertiesDictionary.validate()
        //RelationshipsDictionary.validate()
    }
}