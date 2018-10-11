package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ac.uk.ebi.biostd.submission.procesing.SubFileManager
import ac.uk.ebi.biostd.submission.procesing.SubmissionProcessor
import ebi.ac.uk.model.ISubmission
import ebi.ac.uk.model.submission.SubmitOperation

/**
 * Submission submitter, validates and generateSubFiles submission.
 */
class SubmissionSubmitter(
        private val subProcessor: SubmissionProcessor,
        private val subFileManager: SubFileManager) {

    fun <T : ISubmission> submit(submission: T, submitOperation: SubmitOperation, context: PersistenceContext): T {
        subProcessor.process(submission, submitOperation, context)
        subFileManager.generateSubFiles(submission)
        return submission
    }
}
