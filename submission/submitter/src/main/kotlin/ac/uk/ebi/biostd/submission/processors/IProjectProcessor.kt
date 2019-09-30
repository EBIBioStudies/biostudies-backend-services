package ac.uk.ebi.biostd.submission.processors

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext

interface IProjectProcessor {
    fun process(project: ExtendedSubmission, context: PersistenceContext)
}
