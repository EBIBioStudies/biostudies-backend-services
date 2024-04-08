package ac.uk.ebi.biostd.security.domain.exception

class PermissionsUserDoesNotExistsException(
    userEmail: String,
) : RuntimeException("The user $userEmail does not exist")

class PermissionsAccessTagDoesNotExistsException(
    accessTagName: String,
) : RuntimeException("The accessTag $accessTagName does not exist")
