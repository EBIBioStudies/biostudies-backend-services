package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class LinkConverter(private val attributeConverter: AttributeConverter) : Converter<DocLink, Document> {
    override fun convert(link: DocLink): Document {
        val linkDoc = Document()
        linkDoc[classField] = clazz
        linkDoc[linkDocUrl] = link.url
        linkDoc[linkDocAttributes] = link.attributes.map { attributeConverter.convert(it) }
        return linkDoc
    }

    companion object {
        val clazz: String = DocLink::class.java.canonicalName
        const val linkDocUrl = "url"
        const val linkDocAttributes = "attributes"
    }
}
