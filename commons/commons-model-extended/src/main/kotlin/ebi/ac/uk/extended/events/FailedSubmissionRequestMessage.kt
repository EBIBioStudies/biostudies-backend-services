package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty

data class FailedSubmissionRequestMessage(
    @JsonProperty("accNo")
    val accNo: String,

    @JsonProperty("version")
    val version: Int,
)
