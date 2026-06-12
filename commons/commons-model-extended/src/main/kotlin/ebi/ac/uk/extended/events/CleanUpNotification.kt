package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

const val CLEAN_UP_NOTIFICATION = "User Space Clean Up Notification"

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
) : Serializable
