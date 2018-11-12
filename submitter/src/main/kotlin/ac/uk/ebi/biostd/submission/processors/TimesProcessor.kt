package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.functions.now
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constans.SubFields
import ebi.ac.uk.model.extensions.releaseTime
import java.time.Instant

class TimesProcessor : SubmissionProcessor {

    override fun process(user: User, submission: Submission, persistenceContext: PersistenceContext) {
        val time = Times(submission.releaseTime ?: now(), now(), now())

        submission[SubFields.RELEASE_TIME] = time
        submission[SubFields.CREATION_TIME] = time
        submission[SubFields.MODIFICATION_TIME] = time
    }
}

class Times(
        private val releaseTime: Instant,
        private val creationTime: Instant,
        private val modificationTime: Instant) {

    operator fun component1(): Instant = releaseTime
    operator fun component2(): Instant = creationTime
    operator fun component3(): Instant = modificationTime
}
