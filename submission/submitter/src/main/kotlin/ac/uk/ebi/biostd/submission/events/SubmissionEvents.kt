package ac.uk.ebi.biostd.submission.events

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.User
import io.reactivex.subjects.PublishSubject

class SuccessfulSubmission(
    val user: User,
    val submission: ExtSubmission
)

object SubmissionEvents {
    val successfulSubmission by lazy { PublishSubject.create<SuccessfulSubmission>() }
}
