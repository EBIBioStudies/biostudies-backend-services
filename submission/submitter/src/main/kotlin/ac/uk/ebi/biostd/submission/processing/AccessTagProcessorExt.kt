package ac.uk.ebi.biostd.submission.processing

import ebi.ac.uk.model.ExtendedSubmission

/**
 * Add all parent submission tags to submitted submission.
 */
fun SubmissionSubmitService.parentTags(submission: ExtendedSubmission): List<String> =
    context.getParentAccessTags(submission).filterNot { it == "Public" }
