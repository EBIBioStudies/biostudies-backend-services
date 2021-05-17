package ebi.ac.uk.api.security

@Suppress("LongParameterList")
class UserProfile(
    val sessid: String,
    val email: String,
    val username: String?,
    val secret: String,
    val fullname: String,
    val superuser: Boolean,
    val allow: List<String>,
    val deny: List<String>,
    val aux: ProfileAuxInfo
)

class ProfileAuxInfo(val orcid: String)
