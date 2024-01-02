package its.model.definition.compat

import its.model.Utils.plus
import its.model.definition.Domain
import its.model.definition.rdf.DomainRDFFiller
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import java.io.File
import java.io.Reader
import java.net.URL

/**
 * Построение модели домена на основе словарей и RDF представления данных о домене
 *
 * Создан для удобства. Основную логику см. в [DomainDictionariesBuilder] и [DomainRDFFiller]
 */
object DomainDictionariesRDFBuilder {
    /**
     * Посроить модель домена на основе словарей
     * @param enumsDictReader reader для словаря перечислений
     * @param classesDictReader reader для словаря классов
     * @param propertiesDictReader reader для словаря свойств
     * @param relationshipsDictReader reader для словаря отношений
     * @param rdfModel RDF-модель с данными
     * @param rdfFillOptions Опции для чтения RDF-данных
     */
    @JvmStatic
    fun buildDomain(
        enumsDictReader: Reader,
        classesDictReader: Reader,
        propertiesDictReader: Reader,
        relationshipsDictReader: Reader,
        rdfModel: Model,
        rdfFillOptions: Set<DomainRDFFiller.Option> = setOf(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
    ): Domain {
        val domain = DomainDictionariesBuilder.buildDomain(
            enumsDictReader,
            classesDictReader,
            propertiesDictReader,
            relationshipsDictReader
        )
        DomainRDFFiller.fillDomain(domain, rdfModel, rdfFillOptions)
        return domain
    }

    /**
     * Посроить модель домена на основе данных, взятых из директории [directoryUrl]
     *
     * Ожидается, что словари являются файломи в данной директории и называются
     * `'enums.csv'`, `'classes.csv'`, `'properties.csv'` и `'relationships.csv'` соответственно.
     * RDF-данные аналогично читаются из turtle-файла `'domain.ttl'`
     */
    @JvmStatic
    fun buildDomain(
        directoryUrl: URL,
        rdfFillOptions: Set<DomainRDFFiller.Option> = setOf(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
    ): Domain {
        val domain = DomainDictionariesBuilder.buildDomain(directoryUrl)
        val rdfModel = ModelFactory.createDefaultModel()
            .read((directoryUrl + "domain.ttl").openStream().buffered(), null, "TTL")
        DomainRDFFiller.fillDomain(domain, rdfModel, rdfFillOptions)
        return domain
    }

    /**
     * Посроить модель домена на основе словарей, взятых из директории [directoryPath]
     *
     * Ожидается, что словари являются файломи в данной директории и называются
     * `'enums.csv'`, `'classes.csv'`, `'properties.csv'` и `'relationships.csv'` соответственно
     * RDF-данные аналогично читаются из turtle-файла `'domain.ttl'`
     */
    @JvmStatic
    fun buildDomain(
        directoryPath: String,
        rdfFillOptions: Set<DomainRDFFiller.Option> = setOf(DomainRDFFiller.Option.NARY_RELATIONSHIPS_OLD_COMPAT),
    ) = buildDomain(File(directoryPath).toURI().toURL(), rdfFillOptions)
}