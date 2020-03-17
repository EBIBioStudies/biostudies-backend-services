package ac.uk.ebi.biostd.submission.events

import ebi.ac.uk.model.User
import io.reactivex.subjects.PublishSubject

class SuccessfulSubmission(
    val user: User,
    val accNo: String,
    val released: Boolean,
    val title: String?,
    val releaseDate: String?
)

object SubmissionEvents {
    val successfulSubmission by lazy { PublishSubject.create<SuccessfulSubmission>() }
}
