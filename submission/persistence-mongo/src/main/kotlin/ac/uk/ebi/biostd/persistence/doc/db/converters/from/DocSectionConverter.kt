package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.NfsDocFileFields.NFS_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileTableFields.DOC_FILE_TABLE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkFields.DOC_LINK_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocLinkTableFields.DOC_LINK_TABLE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.CLASS_FIELD
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.DOC_SEC_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.DOC_SEC_TABLE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_LINKS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_SECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TABLE_SECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_DOC_FILE_CLASS
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import arrow.core.Either
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocSectionConverter(
    private val docAttributeConverter: DocAttributeConverter,
    private val docLinkConverter: DocLinkConverter,
    private val docLinkTableConverter: DocLinkTableConverter,
    private val docFileConverter: DocFileConverter,
    private val docFileTableConverter: DocFileTableConverter,
    private val docFileListConverter: DocFileListConverter
) : Converter<Document, DocSection> {
    override fun convert(source: Document): DocSection = DocSection(
        id = source.getObjectId(SEC_ID),
        accNo = source.getString(SEC_ACC_NO),
        type = source.getString(SEC_TYPE),
        fileList = source.findDoc(SEC_FILE_LIST)?.let { docFileListConverter.convert(it) },
        attributes = source.getDocList(SEC_ATTRIBUTES).map { docAttributeConverter.convert(it) },
        sections = source.getDocList(SEC_SECTIONS).map { toEitherSections(it) },
        files = source.getDocList(SEC_FILES).map { toEitherFiles(it) },
        links = source.getDocList(SEC_LINKS).map { toEitherLinks(it) }
    )

    private fun toEitherLinks(doc: Document) = when (val clazz = doc.getString(CLASS_FIELD)) {
        DOC_LINK_CLASS -> Either.left(docLinkConverter.convert(doc))
        DOC_LINK_TABLE_CLASS -> Either.right(docLinkTableConverter.convert(doc))
        else -> throw IllegalStateException("Expecting $clazz to be one of [$DOC_LINK_CLASS , $DOC_LINK_TABLE_CLASS]")
    }

    private fun toEitherFiles(doc: Document) = when (val clazz = doc.getString(CLASS_FIELD)) {
        NFS_DOC_FILE_CLASS, FIRE_DOC_FILE_CLASS -> Either.left(docFileConverter.convert(doc))
        DOC_FILE_TABLE_CLASS -> Either.right(docFileTableConverter.convert(doc))
        else -> throw IllegalStateException(
            """Expecting $clazz to be one of
                [$NFS_DOC_FILE_CLASS,
                $FIRE_DOC_FILE_CLASS,
                $DOC_FILE_TABLE_CLASS]
            """.trimIndent()
        )
    }

    private fun toEitherSections(doc: Document) = when (val clazz = doc.getString(CLASS_FIELD)) {
        DOC_SEC_CLASS -> Either.left(convert(doc))
        DOC_SEC_TABLE_CLASS -> Either.right(toSectionTable(doc))
        else -> throw IllegalStateException("Expecting $clazz to be one of [$DOC_SEC_CLASS , $DOC_SEC_TABLE_CLASS]")
    }

    private fun toSectionTable(document: Document): DocSectionTable =
        DocSectionTable(sections = document.getDocList(SEC_TABLE_SECTIONS).map { toDocSectionTableRow(it) })

    private fun toDocSectionTableRow(doc: Document) =
        DocSectionTableRow(
            accNo = doc.getString(SEC_ACC_NO),
            type = doc.getString(SEC_TYPE),
            attributes = doc.getDocList(SEC_ATTRIBUTES).map { docAttributeConverter.convert(it) }
        )
}
