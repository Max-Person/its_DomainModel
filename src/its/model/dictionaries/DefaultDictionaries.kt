package its.model.dictionaries

import its.model.models.*

class ClassesDictionary(path: String) : ClassesDictionaryBase<ClassModel>(path, ClassModel::class)
class DecisionTreeVarsDictionary(path: String) : DecisionTreeVarsDictionaryBase<DecisionTreeVarModel>(path, DecisionTreeVarModel::class)
class EnumsDictionary(path: String) : EnumsDictionaryBase<EnumModel>(path, EnumModel::class)
class PropertiesDictionary(path: String) : PropertiesDictionaryBase<PropertyModel>(path, PropertyModel::class)
class RelationshipsDictionary(path: String) : RelationshipsDictionaryBase<RelationshipModel>(path, RelationshipModel::class)
