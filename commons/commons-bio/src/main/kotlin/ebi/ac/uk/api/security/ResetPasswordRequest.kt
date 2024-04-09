package ebi.ac.uk.api.security

import com.fasterxml.jackson.annotation.JsonProperty

class ResetPasswordRequest(
    val email: String,
    val instanceKey: String,
    val path: String,
    @JsonProperty("recaptcha2-response")
    val captcha: String? = null,
)
