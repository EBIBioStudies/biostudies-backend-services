package ebi.ac.uk.api.security

import io.swagger.v3.oas.annotations.media.Schema

@Suppress("LongParameterList")
@Schema(description = "Authenticated user's profile and session token.")
class UserProfile(
    @field:Schema(description = "Session token used as `X-Session-Token` header on subsequent calls", example = "<auth-token>")
    val sessid: String,
    @field:Schema(description = "User email", example = "user@test.org")
    val email: String,
    @field:Schema(description = "User login name", example = "user")
    val username: String?,
    @field:Schema(description = "User secret used to derive private upload paths", example = "the-user-secret")
    val secret: String,
    @field:Schema(description = "User full name", example = "Test User")
    val fullname: String,
    @field:Schema(description = "Whether the user has superuser privileges", example = "true")
    val superuser: Boolean,
    @field:Schema(description = "User ORCID identifier", example = "0000-0001-2345-6789")
    val orcid: String,
    @field:Schema(description = "Collections the user is allowed to submit to", example = "[\"Public\"]")
    val allow: List<String>,
    @field:Schema(description = "Collections the user is denied submission to", example = "[]")
    val deny: List<String>,
    @field:Schema(description = "Upload storage backend for the user (e.g. NFS, FIRE)", example = "NFS")
    val uploadType: String,
)
