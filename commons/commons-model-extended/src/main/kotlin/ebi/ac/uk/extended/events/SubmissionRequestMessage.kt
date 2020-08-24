package ebi.ac.uk.extended.events

import com.fasterxml.jackson.annotation.JsonProperty
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode

class SubmissionRequestMessage(
    @JsonProperty("submission")
    val submission: ExtSubmission,

    @JsonProperty("fileMode")
    val fileMode: FileMode
)
