package ebi.ac.uk.submission.processing.submission

import ac.uk.ebi.biostd.persistence.integration.SubmissionService
import ebi.ac.uk.model.User

// TODO: add proper exceptions
internal fun SubmissionService.getProjectTags(user: User, accNo: String?): List<String> {
    return when {
        accNo.isNullOrEmpty() -> return emptyList()
        existProject(accNo).not() -> throw Exception()
        canAttach(accNo, user).not() -> throw Exception()
        else -> getProjectAccessTags(accNo)
    }
}
