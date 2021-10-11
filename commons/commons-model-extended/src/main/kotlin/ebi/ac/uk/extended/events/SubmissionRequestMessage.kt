package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import ebi.ac.uk.extended.model.FileMode

data class SubmissionRequestMessage(
    @JsonProperty("accNo")
    val accNo: String,

    @JsonProperty("version")
    val version: Int,

    @JsonProperty("fileMode")
    val fileMode: FileMode,

    @JsonProperty("owner")
    val submitter: String,

    @JsonProperty("draftKey")
    val draftKey: String?
)
