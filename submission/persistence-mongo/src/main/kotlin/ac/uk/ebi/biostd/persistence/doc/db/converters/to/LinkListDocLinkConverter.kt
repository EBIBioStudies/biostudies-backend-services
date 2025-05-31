package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_LINK
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_LINK_LIST_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.LinkListDocLinkFields.LINK_LIST_DOC_LINK_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.CLASS_FIELD
import ac.uk.ebi.biostd.persistence.doc.model.LinkListDocLink
import ac.uk.ebi.biostd.persistence.doc.model.linkListDocLinkDocFileClass
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class LinkListDocLinkConverter(
    private val linkConverter: LinkConverter,
) : Converter<LinkListDocLink, Document> {
    override fun convert(linkListDocLink: LinkListDocLink): Document {
        val linkListDoc = Document()
        linkListDoc[CLASS_FIELD] = linkListDocLinkDocFileClass
        linkListDoc[LINK_LIST_DOC_LINK_ID] = linkListDocLink.id
        linkListDoc[LINK_LIST_DOC_LINK_INDEX] = linkListDocLink.index
        linkListDoc[LINK_LIST_DOC_LINK_LINK_LIST_NAME] = linkListDocLink.linkListName
        linkListDoc[LINK_LIST_DOC_LINK_SUBMISSION_ID] = linkListDocLink.submissionId
        linkListDoc[LINK_LIST_DOC_LINK_SUBMISSION_ACC_NO] = linkListDocLink.submissionAccNo
        linkListDoc[LINK_LIST_DOC_LINK_SUBMISSION_VERSION] = linkListDocLink.submissionVersion
        linkListDoc[LINK_LIST_DOC_LINK_LINK] = linkConverter.convert(linkListDocLink.link)

        return linkListDoc
    }
}
