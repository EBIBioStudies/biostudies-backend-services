package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.security.integration.model.api.SecurityUser

data class SubmissionRequest(
    val submission: Submission,
    val user: SecurityUser,
    val files: FilesSource,
    val method: SubmissionMethod
)
