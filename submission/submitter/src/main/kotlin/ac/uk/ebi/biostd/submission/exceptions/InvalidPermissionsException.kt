package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User

/**
 * Generated when user is not allowed to perform an operation.
 */
sealed class InvalidPermissionsException : RuntimeException()

class ProvideAccessNumber(private val user: User) : InvalidPermissionsException() {
    override val message: String
        get() = "{${user.email}} is not allowed to provide accession number directly"
}

class UserCanNotUpdateSubmit(private val submission: ExtendedSubmission) : InvalidPermissionsException() {

    override val message: String
        get() = "{${submission.user.email}} is not allowed to update submission ${submission.accNo}"
}
