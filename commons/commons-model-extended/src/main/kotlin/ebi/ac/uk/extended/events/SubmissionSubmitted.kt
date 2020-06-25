package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty

class SubmissionSubmitted(
    @JsonProperty("accNo")
    val accNo: String,
    @JsonProperty("pagetabUrl")
    val pagetabUrl: String,
    @JsonProperty("extTabUrl")
    val extTabUrl: String
) : java.io.Serializable
