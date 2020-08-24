package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.Instant

class SubmissionSubmitted(
    @JsonProperty("accNo")
    val accNo: String,

    @JsonProperty("eventTime")
    val eventTime: Instant,

    @JsonProperty("uiUrl")
    val uiUrl: String,

    @JsonProperty("pagetabUrl")
    val pagetabUrl: String,

    @JsonProperty("extTabUrl")
    val extTabUrl: String,

    @JsonProperty("extUserUrl")
    val extUserUrl: String
) : Serializable
