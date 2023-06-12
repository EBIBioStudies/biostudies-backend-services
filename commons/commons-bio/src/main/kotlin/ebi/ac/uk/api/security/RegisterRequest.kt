package ebi.ac.uk.api.security

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import javax.validation.constraints.Email

@Suppress("LongParameterList")
class RegisterRequest(
    val name: String,

    @JsonDeserialize(using = EmailDeserializer::class)
    @field:Email(message = "The provided email is not valid")
    val email: String,

    val password: String,
    var instanceKey: String? = null,
    var path: String? = null,
    var orcid: String? = null,
    val notificationsEnabled: Boolean = false,

    @JsonProperty("recaptcha2-response")
    val captcha: String? = null
)

class CheckUserRequest(
    @JsonDeserialize(using = EmailDeserializer::class)
    @field:Email(message = "The provided email is not valid")
    val userEmail: String,

    val userName: String,
)

class EmailDeserializer : StdDeserializer<String>(String::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): String {
        return _parseString(p, ctxt).lowercase()
    }
}
