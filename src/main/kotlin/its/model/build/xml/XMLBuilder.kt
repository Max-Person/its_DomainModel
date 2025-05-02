package its.model.build.xml

import its.model.build.xml.XMLBuilder.BuildForTags
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import java.lang.reflect.InvocationTargetException
import javax.management.modelmbean.XMLParseException
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.isAccessible

/**
 * Строитель объектов на основе XML
 *
 * Для построения объектов используются методы класса, отмеченные аннотацией [BuildForTags],
 * каждый из которых определяет поведение по построению объектов из некоторого набора XML элементов
 */
abstract class XMLBuilder<Context : ElementBuildContext, Build : Any> {

    //--- Построение ---

    /**
     * Создать объект на основе XML элемента
     */
    fun buildFromElement(el: Element): Build {
        return try {
            if (canBuildFrom(el)) {
                val converter = methodMap[el.nodeName]!!
                val buildingClass = converter.buildClass
                val context = createBuildContext(el, buildingClass)
                converter.convert(context)
            } else {
                val context = createBuildContext(el, defaultBuildingClass)
                buildDefault(context)
            }
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    }

    protected fun canBuildFrom(el: Element): Boolean {
        return methodMap.containsKey(el.nodeName)
    }

    /**
     * Распарсить XML и создать из него объект
     * @param input источник XML данных
     */
    fun buildFrom(input: InputSource): Build {
        // Создаем DocumentBuilder
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        // Создаем DOM документ
        val document = documentBuilder.parse(input)

        // Получаем корневой элемент документа
        val xml: Element = document.documentElement

        // Строим
        return buildFromElement(xml)
    }

    /**
     * Распарсить XML строку и создать из нее объект
     * @param string строковое представление XML
     */
    fun buildFromXMLString(string: String) = buildFrom(InputSource(StringReader(string)))

    /**
     * Распарсить XML файл и создать из нее объект
     * @param path путь к XML файлу
     */
    fun buildFromXMLFile(path: String) = buildFrom(InputSource(path))

    //---- Утилитарные методы ---------

    protected fun Element.getChildren(): List<Element> {
        val out = mutableListOf<Element>()
        var child = this.firstChild
        while (child != null) {
            if (child.nodeType == Node.ELEMENT_NODE) {
                out.add(child as Element)
            }
            child = child.nextSibling
        }
        return out
    }

    protected fun Element.findChild(): Element? {
        return getChildren().firstOrNull()
    }

    protected fun Element.getRequiredChild(): Element {
        return findChild().orElseBuildErr("$this needs to have at least one child tag")
    }

    protected fun Element.getChildren(tagName: String): List<Element> {
        return getChildren().filter { it.tagName.equals(tagName) }
    }

    protected fun Element.findChild(tagName: String): Element? {
        return getChildren(tagName).firstOrNull()
    }

    protected fun ElementBuildContext.getRequiredChild(tagName: String): Element {
        return findChild(tagName)
            .orElseBuildErr("$this needs to have a child tag '$tagName'")
    }

    protected fun ElementBuildContext.getSeveralByWrapper(wrapper: String): List<Element> {
        return findChild(wrapper)?.getChildren()
            .orElseBuildErr("$this needs to have a nested wrapper tag '$wrapper'")
    }

    protected fun ElementBuildContext.findSingleByWrapper(wrapper: String): Element? {
        return findChild(wrapper)?.findChild()
    }

    protected fun ElementBuildContext.getRequiredSingleByWrapper(wrapper: String): Element {
        return getSeveralByWrapper(wrapper).firstOrNull()
            ?: throw createException("Wrapper tag '$wrapper' for $this should not be empty")
    }

    protected fun Element.findAttribute(attr: String): String? {
        return if (this.hasAttribute(attr)) this.getAttribute(attr) else null
    }

    protected fun <T> T?.orElseBuildErr(errMessage: String): T {
        return this ?: throw createException(errMessage)
    }

    protected fun ElementBuildContext.getRequiredAttribute(attr: String): String {
        return this.findAttribute(attr)
            .orElseBuildErr("$this needs to have a '$attr' attribute")
    }

    //---- Ошибки ----

    protected fun checkXML(condition: Boolean, message: String) {
        if (!condition) throw createException(message)
    }

    protected abstract fun createException(message: String): XMLBuildException

    //---- Построение (Скрытая часть)

    /**
     * Аннотация, помечающая метод как **функцию построения** для тегов в списке [values]
     * Только методы, отмеченные данной аннотацией, будут использоваться при построении объектов
     * @param values список названий тегов, для которых будет использоваться данный метод
     */
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    protected annotation class BuildForTags(val values: Array<String>)

    /**
     * Аннотация, помечающая класс объектов, строящийся в **функции построения**.
     * Данный класс помещается в контекст построения при вызове данной функции.
     * В случае, если функция не отмечена данной аннотацией, в ее контекст помещается [defaultBuildingClass].
     * Данная аннотация не имеет никакого эффекта на методы, не являющиеся функциями построения (не отмеченные [BuildForTags])
     * @param clazz строящийся в методе класс объектов
     */
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    protected annotation class BuildingClass(val clazz: KClass<*>)

    private val methodMap: Map<String, ConverterMethod> =
        this::class.members
            .filter { method -> method.findAnnotation<BuildForTags>() != null }
            .map { method ->
                val buildAnnotation = method.findAnnotation<BuildingClass>()
                val buildClass = buildAnnotation?.clazz ?: defaultBuildingClass
                method.findAnnotation<BuildForTags>()!!.values.map { tagName ->
                    tagName to ConverterMethod(buildClass, createConverter(method))
                }
            }
            .flatten().toMap()

    private fun createConverter(method: KCallable<*>): (Context) -> Build {
        require(!method.isAbstract) {
            "Cannot use abstract callable '${method.name}' in ${this::class.simpleName} for building objects from XML"
        }
        require(method.typeParameters.isEmpty()) {
            "Cannot use callable '${method.name}' in ${this::class.simpleName} for building objects from XML as it uses generics"
        }
        require(
            if (method.instanceParameter != null) method.parameters.size == 2
            else method.parameters.size == 1
        ) {
            "Callable '${method.name}' in ${this::class.simpleName} must have just one regular parameter - " +
                    "a ElementBuildContext - to be used for XML building"
        }
        method.isAccessible = true
        return if (method.instanceParameter != null) { c: Context -> method.call(this, c) as Build }
        else { c: Context -> method.call(c) as Build }

    }

    /**
     * Создать контекст для построения объекта.
     * В данный контекст можно сложить всю необходимую для парсинга информацию (например, дочерние объекты)
     */
    protected abstract fun createBuildContext(el: Element, buildClass: KClass<*>): Context

    /**
     * Класс, используемый в контекстах построения, если метод не отмечен аннотацией [BuildingClass]
     */
    protected open val defaultBuildingClass: KClass<*>
        get() = Any::class

    /**
     * Создать объект для тега, которому не соответствует ни одна из определенных функций построения;
     *
     * Реализация по умолчанию выкидывает ошибку [createException] о невозможности построить объект
     */
    protected open fun buildDefault(el: Context): Build {
        throw createException("No build functions exist for tags '${el.nodeName}' in ${this::class.simpleName}.")
    }

    private inner class ConverterMethod(
        val buildClass: KClass<*>,
        val converter: (Context) -> Build,
    ) {
        fun convert(c: Context) = converter.invoke(c)
    }
}

open class XMLBuildException : XMLParseException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Exception) : super(cause, message)
}

open class ElementBuildContext(
    val el: Element,
    var buildClass: KClass<*>,
) : Element by el {
    override fun toString() = "Element '${el.tagName}' used to build a '${buildClass.simpleName}' object"
}

