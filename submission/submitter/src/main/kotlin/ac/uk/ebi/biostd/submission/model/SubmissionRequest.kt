package ac.uk.ebi.biostd.submission.model

import ac.uk.ebi.biostd.persistence.integration.FileMode
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.security.integration.model.api.SecurityUser

data class SubmissionRequest(
    val submission: Submission,
    val submitter: SecurityUser,
    val sources: FilesSource,
    val method: SubmissionMethod,
    val mode: FileMode
)
