package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@Suppress("LongParameterList", "SerialVersionUIDInSerializableClass")
class CleanUpNotification(
    @field:JsonProperty("email")
    val email: String,
    @field:JsonProperty("username")
    val username: String,
    @field:JsonProperty("lastActivityDate")
    val lastActivityDate: String,
    @field:JsonProperty("cleanUpDate")
    val cleanUpDate: String,
    @field:JsonProperty("emailSubject")
    val emailSubject: String,
    @field:JsonProperty("emailTemplate")
    val emailTemplate: String,
    @field:JsonProperty("type")
    val type: String,
) : Serializable
