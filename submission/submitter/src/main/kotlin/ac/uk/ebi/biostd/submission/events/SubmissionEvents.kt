package ac.uk.ebi.biostd.submission.events

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.User
import io.reactivex.subjects.PublishSubject
import java.util.Objects

class SuccessfulSubmission(
    val user: User,
    val submission: ExtSubmission
) {
    override fun equals(other: Any?) = when {
        other !is SuccessfulSubmission -> false
        other === this -> true
        else -> Objects.equals(user, other.user)
            .and(Objects.equals(submission, other.submission))
    }

    override fun hashCode(): Int = Objects.hash(user, submission)
}

object SubmissionEvents {
    val successfulSubmission by lazy { PublishSubject.create<SuccessfulSubmission>() }
}
