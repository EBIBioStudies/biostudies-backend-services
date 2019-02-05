package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User

/**
 * Generated when user is not allow to perform an operation.
 */
sealed class InvalidPermissionsException : RuntimeException()

class ProvideAccessNumber(private val user: User) : InvalidPermissionsException() {
    override val message: String
        get() = "{${user.email}} is not allow to provide accession number directly"
}

class UserCanNotUpdateSubmit(private val submission: ExtendedSubmission) : InvalidPermissionsException() {

    override val message: String
        get() = "{${submission.user.email}} is not allow to update submission ${submission.accNo}"
}
