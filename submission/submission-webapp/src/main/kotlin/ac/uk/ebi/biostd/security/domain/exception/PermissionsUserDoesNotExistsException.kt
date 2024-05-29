package ac.uk.ebi.biostd.security.domain.exception

class PermissionsUserDoesNotExistsException(
    userEmail: String,
) : RuntimeException("The user $userEmail does not exist")
