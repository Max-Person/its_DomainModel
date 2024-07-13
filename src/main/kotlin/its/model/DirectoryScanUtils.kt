package its.model

import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import its.model.DirectoryScanUtils.USE_CLASS_GRAPH_SCAN
import its.model.Utils.plus
import java.net.URL
import java.util.stream.Collectors

/**
 * Методы, связанные со сканированием директорий, задаваемых через URL
 * Т.к. директории задаются URL, то они могут находиться также в ресурсах приложения.
 * Для корректного сканирования ресурсов может использоваться [ClassGraph], см. [USE_CLASS_GRAPH_SCAN]
 */
object DirectoryScanUtils {
    /**
     * Описание найденного в директории файла, подходящего под регулярное выражение
     * @param url URL файла
     * @param regexMatchResult результат сравнения имени файла с регулярным выражением
     */
    data class FileMatch(val url: URL, val regexMatchResult: MatchResult)

    /**
     * Найти в директории файлы, чье собственное имя подходит под [fileNameRegex]
     */
    @JvmStatic
    fun findFilesMatching(directoryUrl: URL, fileNameRegex: Regex): List<FileMatch> {
        return findFiles(directoryUrl)
            .filter { it.filename.matches(fileNameRegex) }
            .map { FileMatch(it.url, fileNameRegex.matchEntire(it.filename)!!) }
    }


    /**
     * Описание найденного в директории файла
     * @param url URL файла
     * @param filename собственное имя файла (без учета предшествующего URL)
     */
    data class FileDescription(val url: URL, val filename: String)

    /**
     * Найти файлы в директории
     */
    @JvmStatic
    fun findFiles(directoryUrl: URL): List<FileDescription> {
        val linesFromStream = directoryUrl.openStream().bufferedReader().lines().collect(Collectors.toList())
        if (linesFromStream.isNotEmpty()) {
            return linesFromStream.map { FileDescription(directoryUrl + it, it) }
        }
        if (USE_CLASS_GRAPH_SCAN) {
            return classGraphScan.allResources
                .map { it.url }
                .filter { it.toString().startsWith(directoryUrl.toString()) }
                .map { FileDescription(it, it.toString().removePrefix(directoryUrl.toString())) }
        }
        return emptyList()
    }

    /**
     * Если true, методы сканирования директорий будут пытаться просканировать также ресурсы приложения
     * с помощью [ClassGraph.scan]
     */
    @JvmField
    var USE_CLASS_GRAPH_SCAN: Boolean = true

    private val classGraphScan: ScanResult by lazy {
        ClassGraph().scan()
    }
}