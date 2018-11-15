package ac.uk.ebi.biostd.submission.exceptions

import java.lang.RuntimeException

const val INVALID_PROJECT_ERROR_MSG = "The project %s doesn't exist"

class InvalidProjectException(private val project: String) : RuntimeException() {
    override val message: String?
        get() = INVALID_PROJECT_ERROR_MSG.format(project)
}
