package ac.uk.ebi.biostd.submission.submitter

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.persistence.PersistenceContext

class ProjectSubmitter {
    fun submit(submission: ExtendedSubmission, context: PersistenceContext): Submission {
        context.saveAccessTag(submission.accNo)
        context.saveSubmission(submission)

        return submission
    }
}
