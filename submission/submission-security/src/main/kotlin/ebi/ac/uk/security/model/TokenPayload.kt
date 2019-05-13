package ebi.ac.uk.security.model

internal data class TokenPayload(val id: Long, val email: String, val fullName: String)
