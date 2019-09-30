package ac.uk.ebi.biostd.submission.validators

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext

interface IProjectValidator {
    fun validate(project: ExtendedSubmission, context: PersistenceContext)
}
