package its.model.dictionaries

import its.model.models.*

class ClassesDictionary : ClassesDictionaryBase<ClassModel>(ClassModel::class)
class DecisionTreeVarsDictionary : DecisionTreeVarsDictionaryBase<DecisionTreeVarModel>(DecisionTreeVarModel::class)
class EnumsDictionary : EnumsDictionaryBase<EnumModel>(EnumModel::class)
class PropertiesDictionary : PropertiesDictionaryBase<PropertyModel>(PropertyModel::class)
class RelationshipsDictionary : RelationshipsDictionaryBase<RelationshipModel>(RelationshipModel::class)
