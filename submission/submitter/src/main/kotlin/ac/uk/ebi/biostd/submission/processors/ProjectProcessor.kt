package ac.uk.ebi.biostd.submission.processors

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.persistence.PersistenceContext

class ProjectProcessor : SubmissionProcessor {
    override fun process(submission: ExtendedSubmission, context: PersistenceContext) {
        submission.addAccessTag(submission.accNo)
    }
}
