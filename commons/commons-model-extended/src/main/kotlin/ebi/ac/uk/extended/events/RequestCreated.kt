package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty

sealed interface RequestMessage {
    val accNo: String
    val version: Int
}

data class RequestCreated(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestLoaded(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestProcessed(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage