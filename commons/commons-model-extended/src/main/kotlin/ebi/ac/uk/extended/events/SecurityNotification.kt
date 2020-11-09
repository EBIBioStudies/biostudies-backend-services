package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class SecurityNotification(
    @JsonProperty("email")
    val email: String,

    @JsonProperty("username")
    val username: String,

    @JsonProperty("activationLink")
    val activationLink: String,

    @JsonProperty("type")
    val type: SecurityNotificationType
) : Serializable

enum class SecurityNotificationType {
    ACTIVATION,
    PASSWORD_RESET
}
