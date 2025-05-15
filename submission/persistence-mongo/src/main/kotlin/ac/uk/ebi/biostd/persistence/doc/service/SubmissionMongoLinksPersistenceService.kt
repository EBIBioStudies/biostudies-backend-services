package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionLinksPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.LinkListDocLinkDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtLink
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubmissionMongoLinksPersistenceService(
    private val linkListDocLinkDocDataRepository: LinkListDocLinkDocDataRepository,
) : SubmissionLinksPersistenceService {
    override fun getReferencedLinks(
        sub: ExtSubmission,
        linkListName: String,
    ): Flow<ExtLink> =
        linkListDocLinkDocDataRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndLinkListName(sub.accNo, 0, linkListName)
            .map { it.link.toExtLink() }
}
