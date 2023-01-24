package its.model.dictionaries

object DictionariesUtil {

    /**
     * Разделитель столбцов в CSV файле словаря
     */
    internal const val COLUMNS_SEPARATOR = '|'

    /**
     * Разделитель элементов списка в ячейке CSV файла словаря
     */
    internal const val LIST_ITEMS_SEPARATOR = ';'

    /**
     * Инициализирует все словари и проверят их валидность
     */
    fun initAllDictionaries(
        classesDictionaryPath: String,
        decisionTreeVarsDictionaryPath: String,
        enumsDictionaryPath: String,
        propertiesDictionaryPath: String,
        relationshipsDictionaryPath: String
    ) {
        ClassesDictionary.init(classesDictionaryPath)
        DecisionTreeVarsDictionary.init(decisionTreeVarsDictionaryPath)
        EnumsDictionary.init(enumsDictionaryPath)
        PropertiesDictionary.init(propertiesDictionaryPath)
        RelationshipsDictionary.init(relationshipsDictionaryPath)

        ClassesDictionary.validate()
        DecisionTreeVarsDictionary.validate()
        EnumsDictionary.validate()
        PropertiesDictionary.validate()
        RelationshipsDictionary.validate()
    }
}