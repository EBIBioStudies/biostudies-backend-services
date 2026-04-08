package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CollectionMongoDataService(
    private val submissionDocDataRepository: SubmissionDocDataRepository,
) : CollectionDataService {
    override fun findCollectionsByAccessTags(tags: List<String>): Flow<BasicSubmission> =
        submissionDocDataRepository
            .getByAccNoInAndVersionGreaterThanAndSectionType(tags, 0, COLLECTION_SECTION_TYPE)
            .map { it.asBasicSubmission(PROCESSED) }

    companion object {
        const val COLLECTION_SECTION_TYPE = "Project"
    }
}
