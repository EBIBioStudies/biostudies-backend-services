package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ExtendedSubmission
import java.time.OffsetDateTime

class TimesProcessor : SubmissionProcessor {

    override fun process(submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        val now = OffsetDateTime.now()
        val previousVersion = persistenceContext.getSubmission(submission.accNo)

        submission.modificationTime = now
        submission.releaseTime = if(submission.releaseTime == null) now else submission.releaseTime
        previousVersion.fold( { submission.creationTime = now }, { submission.creationTime = it.creationTime } )
    }
}
