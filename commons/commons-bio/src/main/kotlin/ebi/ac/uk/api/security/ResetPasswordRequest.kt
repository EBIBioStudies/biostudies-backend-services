package ebi.ac.uk.api.security

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

class ResetPasswordRequest(
    @field:Schema(description = "User email", example = "user@test.org", required = true)
    val email: String,
    @field:Schema(
        description = "Instance key that should process the password reset",
        example = "the-instance-key",
        required = true,
    )
    val instanceKey: String,
    @field:Schema(
        description = "Path to redirect the user in the UI",
        example = "/users/password_reset",
        required = true,
    )
    val path: String,
    @JsonProperty("recaptcha2-response")
    @field:Schema(description = "Re-Captcha validation response", example = "recaptcha-response")
    val captcha: String? = null,
)
