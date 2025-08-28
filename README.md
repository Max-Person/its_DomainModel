 its_DomainModel  - модуль, служащий основой для остальных модулей Compprehensive ITS (its_\*), и отвечающий за создание и хранение необходимых моделей данных о предметной области.  
## Функционал в этом модуле  
- Построение модели предметной области - описание сущностей и их типов, присущих данной предметной области.  
- Построение LOQI-выражений, описывающих действия и рассуждения над моделью предметной области.  
- Построение графов мыслительных процессов, представляющих модель мышления, необходимого для решения некоторой задачи в заданной предметной области.  
- Валидация описанных выше моделей.
  
**Подробнее о функционале этого и других модулей читайте на [вики](https://max-person.github.io/Compprehensive_ITS_wiki/1.-%D0%BC%D0%BE%D0%B4%D0%B5%D0%BB%D0%B8-(its_domainmodel)/%D0%BE%D0%B1-its_domainmodel.html).**

## Установка зависимостей

Как автор данных проектов, **я рекомендую использовать Maven + [JitPack](https://jitpack.io/)  для подключения проектов its_\* как зависимостей в ваших собственных проектах**. Для этого необходимо:

1\. В pom.xml своего проекта указать репозиторий JitPack:
```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```
2\. Также в pom.xml указать репозиторий как зависимость:
```xml
<dependency>
	<groupId>com.github.Max-Person</groupId>
	<artifactId>its_DomainModel</artifactId>
	<version>...</version>
</dependency>
```
- В качестве версии JitPack может принимать название ветки, тег (release tag), или хэш коммита. Для данных проектов я рекомендую указывать тег последней версии (см. [tags](https://github.com/Max-Person/its_DomainModel/tags)), чтобы ваш проект не сломался с обновлением библиотек.


3\. В IntelliJ IDEA надо обновить зависимости Maven (Maven -> Reload All Maven Projects), и все, данный проект настроен для использования в качестве библиотеки.
> [!note]
Обратите внимание, что JitPack собирает нужные артефакты только по запросу - т.е. когда вы подтягиваете зависимость. Это значит, что первое подобное подтягивание скорее всего займет несколько минут - JitPack-у нужно будет время на билд.  
После завершения такого долгого билда, в IDEA может отобразиться надпись "Couldn't aqcuire locks", как будто произошла ошибка - в этом случае просто обновитесь еще раз, это будет быстро.

4\. Вместе с артефактами данной библиотеки всегда доступен ее исходный код, а в нем и документация (kotlindoc/javadoc). **Проект на 90% задокументирован, поэтому смотрите на документацию к используемым вами методам!**  
Для того, чтобы исходный код и документация тоже подтянулись, нужно в IntelliJ IDEA сделать Maven -> Download Sources and/or Documentation -> Download Sources and Documentation

## Примеры использования  
Примеры использования описаны на Java, т.к. я думаю, что вы с большей вероятностью будете использовать именно ее (использование на Kotlin в принципе аналогично, и более просто).  
#### Создание модели предметной области
  
Создать модель предметной области из .loqi файла:  
```java
DomainModel domainModel = DomainLoqiBuilder.buildDomain(new FileReader(filename)); 
```
  
Создать модель предметной области из папки с .csv словарями и RDF файлами:  
```java
DomainModel domainModel = DomainDictionariesRDFBuilder.buildDomain(  
    directoryPath,  
    Collections.emptySet() 
);
```
  
#### Дополнение модели предметной области новыми данными
  
Наполнить существующую модель данными из  .loqi файла:  
```java
domainModel.addMerge(DomainLoqiBuilder.buildDomain(new FileReader(newFilename)))
```
  
Наполнить существующую модель данными из RDF:  
```java
DomainRDFFiller.fillDomain(  
    domainModel,  
    ttlFilePath,  //путь к .ttl файлу с RDF
    Collections.emptySet(), //или Set.of(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT)  
    someTtlBasePrefix //префикс, использующийся в .ttl файле - например RDFUtils.POAS_PREF  
);
```
  
#### Запись модели предметной области в файл
  
Запись в виде LOQI:  
```java
DomainLoqiWriter.saveDomain(  
    domainModel,  
    new FileWriter(filename),  
    Collections.emptySet()  
);
```
  
Запись в виде RDF:  
```java
DomainRDFWriter.saveDomain(  
    domainModel,  
    new FileWriter(filename),  
    someTtlBasePrefix, //префикс, использующийся в .ttl файле - например RDFUtils.POAS_PREF    
    Collections.emptySet() //или Set.of(DomainRDFWriter.Option.NARY_RELATIONSHIPS_OLD_COMPAT)  
);
```
  
#### Создание дерева решений
  
Построение дерева решений из .xml файла  
```java
DecisionTree decisionTree = DecisionTreeXMLBuilder.fromXMLFile(filename);
```
  
### Типичный пример использования моделей
  
```java
//Строим и валидируем составную модель (предметная область + теги + деревья решений)  
DomainSolvingModel domainSolvingModel = new DomainSolvingModel(  
    directoryPath,  
    DomainSolvingModel.BuildMethod.LOQI  
);  
domainSolvingModel.validate();  
  
//Получаем общую модель для под-области  
DomainModel subDomainModel = domainSolvingModel.getMergedTagDomain(someTagName);  
  
//Получаем модель для конкретной ситуации  
DomainModel situationDomain = DomainLoqiBuilder.buildDomain(new FileReader(situationFileName));  
//Объединяем модель ситуации с моделью под-области - делаем ее полной  
situationDomain.addMerge(subDomainModel);  
//Валидируем модель ситуации  
situationDomain.validateAndThrow();  
  
//Получаем дерево решений из составной модели  
DecisionTree decisionTree = domainSolvingModel.getDecisionTree();  
  
//...проводим дальнейшие вычисления (см. its_Reasoner)
```