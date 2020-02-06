package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.User

/**
 * Generated when user is not allowed to perform an operation.
 */
sealed class InvalidPermissionsException(message: String) : RuntimeException(message)

class ProvideAccessNumber(user: String) : InvalidPermissionsException(
    "{$user} is not allowed to provide accession number directly")

class UserCanNotUpdateSubmit(accNo: String, user: String) : InvalidPermissionsException(
    "{$user} is not allowed to update submission $accNo")

class UserCanNotSubmitProjectsException(user: User) : InvalidPermissionsException(
    "The user ${user.email} is not allowed to submit projects")
