package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.LinkListDocLinkRepository
import ac.uk.ebi.biostd.persistence.doc.model.LinkListDocLink
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

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

    suspend fun findAllBySubmissionAccNoAndSubmissionVersionAndLinkListName(
        accNo: String,
        version: Int,
        fileListName: String,
        pageable: Pageable,
    ): Page<LinkListDocLink> {
        val records =
            linkListDocLinkRepository
                .findAllBySubmissionAccNoAndSubmissionVersionAndLinkListNameOrderByIndexAsc(
                    accNo,
                    version,
                    fileListName,
                    pageable,
                )
        val total =
            linkListDocLinkRepository.countBySubmissionAccNoAndSubmissionVersionAndLinkListName(
                accNo,
                version,
                fileListName,
            )
        return PageImpl(records.toList(), pageable, total)
    }
}
