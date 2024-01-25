package its.model.definition.rdf

import org.apache.jena.rdf.model.Resource
import org.apache.jena.util.SplitIRI

object RDFUtils {
    /**
     * POAS префикс
     */
    const val POAS_PREF = "http://www.vstu.ru/poas/code#"

    /**
     * XSD префикс
     */
    const val XSD_PREF = "http://www.w3.org/2001/XMLSchema#"

    /**
     * RDF префикс
     */
    const val RDF_PREF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"

    /**
     * RDFS префикс
     */
    const val RDFS_PREF = "http://www.w3.org/2000/01/rdf-schema#"

    val Resource.name: String
        get() = SplitIRI.localname(this.uri)
}