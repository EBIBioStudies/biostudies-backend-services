package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ac.uk.ebi.biostd.submission.procesing.SubFileManager
import ac.uk.ebi.biostd.submission.procesing.SubmissionProcessor
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constans.SubmitOperation

/**
 * Submission submitter, validates and generateSubFiles submission.
 */
class SubmissionSubmitter(
        private val subProcessor: SubmissionProcessor,
        private val subFileManager: SubFileManager) {

    fun submit(user: User,
               submission: Submission,
               submitOperation: SubmitOperation,
               context: PersistenceContext): Submission {

        subProcessor.process(submission, submitOperation, context)
        subFileManager.generateSubFiles(submission, user)
        return submission
    }
}
