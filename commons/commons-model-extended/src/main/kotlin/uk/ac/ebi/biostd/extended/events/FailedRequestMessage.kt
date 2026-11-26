package uk.ac.ebi.biostd.extended.events

import com.fasterxml.jackson.annotation.JsonProperty

data class FailedRequestMessage(
    @JsonProperty("accNo")
    val accNo: String,
    @JsonProperty("version")
    val version: Int,
)
