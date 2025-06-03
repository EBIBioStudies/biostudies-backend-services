package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_LINK
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_LINK_LIST_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.LinkListDocLink
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocLinkListDocLinkConverter(
    private val docLinkConvert: DocLinkConverter,
) : Converter<Document, LinkListDocLink> {
    override fun convert(source: Document): LinkListDocLink =
        LinkListDocLink(
            id = source.getObjectId(LINK_LIST_DOC_LINK_ID),
            submissionId = source.getObjectId(LINK_LIST_DOC_LINK_SUBMISSION_ID),
            link = docLinkConvert.convert(source.get(LINK_LIST_DOC_LINK_LINK, Document::class.java)),
            linkListName = source.getString(LINK_LIST_DOC_LINK_LINK_LIST_NAME),
            index = source.getInteger(LINK_LIST_DOC_LINK_INDEX),
            submissionAccNo = source.getString(LINK_LIST_DOC_LINK_SUBMISSION_ACC_NO),
            submissionVersion = source.getInteger(LINK_LIST_DOC_LINK_SUBMISSION_VERSION),
        )
}
