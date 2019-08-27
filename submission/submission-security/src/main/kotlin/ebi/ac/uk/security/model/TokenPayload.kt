package ebi.ac.uk.security.model

import java.time.OffsetDateTime

/**
 * Represent the token payload serialization content.
 */
internal data class TokenPayload(
    val id: Long,
    val email: String,
    val fullName: String,
    val creationTime: Long
) {
    constructor(id: Long, email: String, fullName: String) :
        this(id, email, fullName, OffsetDateTime.now().toEpochSecond())
}
