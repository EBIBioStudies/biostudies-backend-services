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

data class RequestIndexed(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestPageTabGenerated(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestLoaded(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestCleaned(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestCheckedReleased(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestFilesCopied(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestPersisted(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage

data class RequestFinalized(
    @JsonProperty("accNo") override val accNo: String,
    @JsonProperty("version") override val version: Int,
) : RequestMessage
