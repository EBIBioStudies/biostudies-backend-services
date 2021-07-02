package ac.uk.ebi.biostd.submission.exceptions

/**
 * Generated when user is not allowed to perform an operation.
 */
sealed class InvalidPermissionsException(message: String) : RuntimeException(message)

class UserCanNotProvideAccessNumber(
    user: String
) : InvalidPermissionsException("The user {$user} is not allowed to provide accession number directly")

class UserCanNotUpdateSubmit(
    accNo: String,
    user: String
) : InvalidPermissionsException("The user {$user} is not allowed to update the submission $accNo")

class UserCanNotSubmitProjectsException(
    user: String
) : InvalidPermissionsException("The user $user is not allowed to submit collections")

class UserCanNotSubmitToProjectException(
    user: String,
    project: String
) : InvalidPermissionsException("The user $user is not allowed to submit to $project project")

class UserCanNotDelete(
    accNo: String,
    user: String
) : InvalidPermissionsException("The user {$user} is not allowed to delete the submission $accNo")
