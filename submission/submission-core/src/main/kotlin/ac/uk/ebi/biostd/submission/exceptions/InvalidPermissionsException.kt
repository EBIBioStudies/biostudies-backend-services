package ac.uk.ebi.biostd.submission.exceptions

/**
 * Generated when user is not allowed to perform an operation.
 */
sealed class InvalidPermissionsException(message: String) : RuntimeException(message)

class UserCanNotProvideAccessNumber(
    user: String,
) : InvalidPermissionsException("The user $user is not allowed to provide accession number directly")

class UserCanNotUpdateSubmit(
    accNo: String,
    user: String,
) : InvalidPermissionsException("The user $user is not allowed to update the submission $accNo")

class UserCanNotSubmitCollectionsException(
    user: String,
) : InvalidPermissionsException("The user $user is not allowed to submit collections")

class UserCanNotSubmitToCollectionException(
    user: String,
    collection: String,
) : InvalidPermissionsException("The user $user is not allowed to submit to $collection collection")

class UserCanNotDeleteSubmission(
    user: String,
    accNo: String,
) : InvalidPermissionsException("The user $user is not allowed to delete the submission $accNo")

class UserCanNotDeleteSubmissions(
    user: String,
    accNos: List<String>,
) : InvalidPermissionsException("The user $user is not allowed to delete the submissions ${accNos.joinToString(", ")}")

class UserCanNotRelease(
    accNo: String,
    user: String,
) : InvalidPermissionsException("The user $user is not allowed to release the submission $accNo")
