package ac.uk.ebi.biostd.submission.exceptions

/**
 * Generated when user is not allowed to perform an operation.
 */
sealed class InvalidPermissionsException(message: String) : RuntimeException(message)

class ProvideAccessNumber(
    user: String
) : InvalidPermissionsException("{$user} is not allowed to provide accession number directly")

class UserCanNotUpdateSubmit(
    accNo: String,
    user: String
) : InvalidPermissionsException("{$user} is not allowed to update submission $accNo")

class UserCanNotSubmitProjectsException(
    user: String
) : InvalidPermissionsException("The user $user is not allowed to submit projects")

class UserCanNotSubmitToProjectException(
    user: String,
    project: String
) : InvalidPermissionsException("The user $user is not allowed to submit to $project project")
