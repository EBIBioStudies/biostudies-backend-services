package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@Suppress("SerialVersionUIDInSerializableClass")
class CleanUpNotification(
    @JsonProperty("email")
    val email: String,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("lastActivityDate")
    val lastActivityDate: String,
    @JsonProperty("cleanUpDate")
    val cleanUpDate: String,
    @JsonProperty("emailSubject")
    val emailSubject: String,
    @JsonProperty("emailTemplate")
    val emailTemplate: String,
    @JsonProperty("type")
    val type: String,
) : Serializable
