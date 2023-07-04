package ebi.ac.uk.api.security

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email

@Suppress("LongParameterList")
class RegisterRequest(
    val name: String,

    @field:Email(message = "The provided email is not valid")
    val email: String,

    val password: String,
    var instanceKey: String? = null,
    var path: String? = null,
    var orcid: String? = null,
    val notificationsEnabled: Boolean = false,
    val userFolderType: String? = null,

    @JsonProperty("recaptcha2-response")
    val captcha: String? = null,
)
