package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@Suppress("SerialVersionUIDInSerializableClass")
class UrgentNotification(
    @JsonProperty("subject")
    val subject: String,
    @JsonProperty("content")
    val content: String,
) : Serializable
