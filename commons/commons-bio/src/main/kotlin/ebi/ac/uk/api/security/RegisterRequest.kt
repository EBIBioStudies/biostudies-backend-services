package ebi.ac.uk.api.security

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email

@Suppress("LongParameterList")
class RegisterRequest(
    @field:Schema(description = "User name", example = "Test User", required = true)
    val name: String,
    @field:Email(message = "The provided email is not valid")
    @field:Schema(description = "User email", example = "user@test.org", required = true)
    val email: String,
    @field:Schema(description = "Password for the new account", example = "123456", required = true)
    val password: String,
    @field:Schema(description = "Instance key that should process the registration", example = "the-instance-key")
    var instanceKey: String? = null,
    @field:Schema(description = "Path to redirect the user in the UI", example = "/users/register")
    var path: String? = null,
    @field:Schema(description = "User ORCID identifier", example = "0000-0001-2345-6789")
    var orcid: String? = null,
    @field:Schema(
        description = "Whether the new user receives BioStudies notifications. Disabled by default.",
        example = "false",
    )
    val notificationsEnabled: Boolean = false,
    @field:Schema(description = "Storage mode for the user's files", example = "NFS")
    val storageMode: String? = null,
    @JsonProperty("recaptcha2-response")
    @field:Schema(description = "Re-Captcha validation response", example = "recaptcha-response")
    val captcha: String? = null,
)

class CheckUserRequest(
    @field:Email(message = "The provided email is not valid")
    @field:Schema(description = "The email to validate", example = "user@test.org", required = true)
    val userEmail: String,
    @field:Schema(
        description = "Name used to create the user if it doesn't exist",
        example = "Test User",
        required = true,
    )
    val userName: String,
)
