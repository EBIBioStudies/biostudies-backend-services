package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@Suppress("SerialVersionUIDInSerializableClass")
class SecurityNotification(
    @JsonProperty("email")
    val email: String,

    @JsonProperty("username")
    val username: String,

    @JsonProperty("activationCode")
    val activationCode: String,

    @JsonProperty("activationLink")
    val activationLink: String,

    @JsonProperty("type")
    val type: SecurityNotificationType
) : Serializable

enum class SecurityNotificationType {
    ACTIVATION,
    ACTIVATION_BY_EMAIL,
    PASSWORD_RESET
}
