package ebi.ac.uk.security.model

data class TokenPayload(val id: Long, val email: String, val fullName: String?, val login: String)
