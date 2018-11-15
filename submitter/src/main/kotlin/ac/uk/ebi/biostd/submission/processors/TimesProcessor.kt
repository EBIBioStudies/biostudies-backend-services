package ac.uk.ebi.biostd.submission.processors

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext
import java.time.OffsetDateTime

class TimesProcessor : SubmissionProcessor {

    override fun process(submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        val now = OffsetDateTime.now()
        val previousVersion = persistenceContext.getSubmission(submission.accNo)

        submission.modificationTime = now
        submission.releaseTime = if (submission.releaseTime == null) now else submission.releaseTime
        previousVersion.fold({ submission.creationTime = now }, { submission.creationTime = it.creationTime })
    }
}
