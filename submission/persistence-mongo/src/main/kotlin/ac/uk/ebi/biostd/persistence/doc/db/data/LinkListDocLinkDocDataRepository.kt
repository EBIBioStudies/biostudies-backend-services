package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.LinkListDocLinkRepository
import ac.uk.ebi.biostd.persistence.doc.model.LinkListDocLink
import kotlinx.coroutines.flow.Flow

class LinkListDocLinkDocDataRepository(
    private val linkListDocLinkRepository: LinkListDocLinkRepository,
) : LinkListDocLinkRepository by linkListDocLinkRepository {
    fun findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndLinkListName(
        accNo: String,
        version: Int,
        linkListName: String,
    ): Flow<LinkListDocLink> =
        linkListDocLinkRepository
            .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndLinkListNameOrderByIndexAsc(
                accNo,
                version,
                linkListName,
            )

    fun findByLinkList(
        accNo: String,
        version: Int,
        linkListName: String,
    ): Flow<LinkListDocLink> =
        linkListDocLinkRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndLinkListNameOrderByIndexAsc(
                accNo,
                version,
                linkListName,
            )
}
