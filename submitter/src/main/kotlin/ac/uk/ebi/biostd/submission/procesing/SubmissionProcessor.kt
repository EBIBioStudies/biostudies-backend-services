package ac.uk.ebi.biostd.submission.procesing

import ac.uk.ebi.biostd.submission.helpers.AccNoProcessor
import ac.uk.ebi.biostd.submission.helpers.RelPathProcessor
import ac.uk.ebi.biostd.submission.helpers.TimesProcessor
import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ISubmission
import ebi.ac.uk.model.submission.SubmitOperation

/**
 * Class in charge of calculate.
 */
class SubmissionProcessor(
        private val accNoProcessor: AccNoProcessor,
        private val pathProcessor: RelPathProcessor,
        private val timesProcessor: TimesProcessor) {

    fun process(submission: ISubmission, operation: SubmitOperation, context: PersistenceContext) {
        val accNo = accNoProcessor.getAccNo(submission, context)
        val relPath = pathProcessor.getRelPath(accNo)
        val accessTags = context.getParentAccessTags(submission)
        val (releaseTime, creationTime, modificationTime) = timesProcessor.processSubmission(submission, operation)

        submission.apply {
            this.accNo = accNo.toString()
            this.relPath = relPath
            this.accessTags = accessTags.toMutableSet()
            this.releaseTime = releaseTime
            this.creationTime = creationTime
            this.modificationTime = modificationTime
        }
    }
}
