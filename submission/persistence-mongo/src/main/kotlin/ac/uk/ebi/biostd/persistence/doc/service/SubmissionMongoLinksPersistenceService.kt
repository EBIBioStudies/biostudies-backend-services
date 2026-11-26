package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionLinksPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.LinkListDocLinkDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.to.toExtLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.extended.model.ExtSubmission

class SubmissionMongoLinksPersistenceService(
    private val linkListDocLinkDocDataRepository: LinkListDocLinkDocDataRepository,
) : SubmissionLinksPersistenceService {
    override fun getReferencedLinks(
        accNo: String,
        linkListName: String,
    ): Flow<ExtLink> =
        linkListDocLinkDocDataRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndLinkListName(accNo, 0, linkListName)
            .map { it.link.toExtLink() }

    override suspend fun getReferencedLinks(
        sub: ExtSubmission,
        linkListName: String,
        pageable: Pageable,
    ): Page<ExtLink> =
        linkListDocLinkDocDataRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndLinkListName(
                sub.accNo,
                sub.version,
                linkListName,
                pageable,
            ).map { it.link.toExtLink() }
}
