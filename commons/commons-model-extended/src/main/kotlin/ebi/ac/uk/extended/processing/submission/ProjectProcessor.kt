package ebi.ac.uk.extended.processing.submission

import ebi.ac.uk.extended.integration.SubmissionService
import ebi.ac.uk.model.User

class ProjectProcessor(private val submissionService: SubmissionService) {

    fun getProjectTags(user: User, accNo: String?): List<String> {
        return when {
            accNo.isNullOrEmpty() -> return emptyList()
            submissionService.existProject(accNo).not() -> throw Exception()
            submissionService.canAttach(accNo).not() -> throw Exception()
            else -> submissionService.getProjectAccessTags(accNo)
        }
    }
}
