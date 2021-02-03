package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.ProjectDataService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission

class ProjectMongoDataService(
    private val submissionDocDataRepository: SubmissionDocDataRepository
) : ProjectDataService {

    override fun findProjectsByAccessTags(tags: List<String>): List<BasicSubmission> {
        return submissionDocDataRepository
            .getByAccNoInAndVersionGreaterThan(tags, 0)
            .map { it.asBasicSubmission() }
    }
}
