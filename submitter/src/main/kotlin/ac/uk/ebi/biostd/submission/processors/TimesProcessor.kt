package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
import java.time.OffsetDateTime

class TimesProcessor : SubmissionProcessor {

    override fun process(user: User, submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        val time = OffsetDateTime.now()

        submission.releaseTime = time
        submission.creationTime = time
        submission.modificationTime = time
    }
}
