package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class SubmissionSubmitted(
    @JsonProperty("accNo")
    val accNo: String,

    @JsonProperty("ownerFullName")
    val ownerFullName: String,

    @JsonProperty("notificationsEnabled")
    val notificationsEnabled: Boolean,

    @JsonProperty("uiUrl")
    val uiUrl: String,

    @JsonProperty("pagetabUrl")
    val pagetabUrl: String,

    @JsonProperty("extTabUrl")
    val extTabUrl: String
) : Serializable
