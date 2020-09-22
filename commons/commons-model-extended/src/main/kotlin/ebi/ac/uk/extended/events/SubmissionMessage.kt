package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

class SubmissionMessage(
    @JsonProperty("accNo")
    val accNo: String,

    @JsonProperty("pagetabUrl")
    val pagetabUrl: String,

    @JsonProperty("extTabUrl")
    val extTabUrl: String,

    @JsonProperty("extUserUrl")
    val extUserUrl: String,

    @JsonProperty("eventTime")
    val eventTime: String
) : Serializable
