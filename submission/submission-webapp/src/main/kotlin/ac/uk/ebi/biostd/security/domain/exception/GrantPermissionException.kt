package ac.uk.ebi.biostd.security.domain.exception

class GrantPermissionException(
    user: String,
    accNo: String,
) : RuntimeException("User $user can't grant permissions over $accNo")

class RevokePermissionException(
    user: String,
    accNo: String,
) : RuntimeException("User $user can't revoke permissions over $accNo")
