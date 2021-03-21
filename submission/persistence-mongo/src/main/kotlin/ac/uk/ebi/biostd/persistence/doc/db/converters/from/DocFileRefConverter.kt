package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileRefFields
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.core.convert.converter.Converter

class DocFileRefConverter : Converter<Document, DocFileRef> {
    override fun convert(source: Document): DocFileRef =
        DocFileRef(ObjectId(source.getString(DocFileRefFields.FILE_REF_DOC_FILE_ID)))
}
