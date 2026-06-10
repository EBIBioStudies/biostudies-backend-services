package ebi.ac.uk.api.security

import java.time.LocalDateTime

@Suppress("LongParameterList")
class UserProfile(
    val sessid: String,
    val email: String,
    val username: String?,
    val secret: String,
    val fullname: String,
    val superuser: Boolean,
    val orcid: String,
    val allow: List<String>,
    val deny: List<String>,
    val uploadType: String,
    val lastActivity: LocalDateTime,
)
