package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission

class CollectionMongoDataService(
    private val submissionDocDataRepository: SubmissionDocDataRepository
) : CollectionDataService {

    override fun findProjectsByAccessTags(tags: List<String>): List<BasicSubmission> {
        return submissionDocDataRepository
            .getByAccNoInAndVersionGreaterThan(tags, 0)
            .map { it.asBasicSubmission() }
    }
}
