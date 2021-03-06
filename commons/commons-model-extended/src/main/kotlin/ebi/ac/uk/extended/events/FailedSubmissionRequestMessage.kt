package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import ebi.ac.uk.extended.model.FileMode

data class FailedSubmissionRequestMessage(
    @JsonProperty("accNo")
    val accNo: String,

    @JsonProperty("version")
    val version: Int,

    @JsonProperty("fileMode")
    val fileMode: FileMode,

    @JsonProperty("draftKey")
    val draftKey: String?,

    @JsonProperty("errorMessage")
    val errorMessage: String?
)
