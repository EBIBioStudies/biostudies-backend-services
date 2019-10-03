package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User

/**
 * Generated when user is not allowed to perform an operation.
 */
sealed class InvalidPermissionsException(message: String) : RuntimeException(message)

class ProvideAccessNumber(user: User) : InvalidPermissionsException(
    "{${user.email}} is not allowed to provide accession number directly")

class UserCanNotUpdateSubmit(submission: ExtendedSubmission) : InvalidPermissionsException(
    "{${submission.user.email}} is not allowed to update submission ${submission.accNo}")

class UserCanNotSubmitProjectsException(user: User) : InvalidPermissionsException(
    "The user ${user.email} is not allowed to submit projects")
