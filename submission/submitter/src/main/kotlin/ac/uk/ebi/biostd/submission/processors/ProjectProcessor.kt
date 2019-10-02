package ac.uk.ebi.biostd.submission.processors

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.persistence.PersistenceContext

class ProjectProcessor : IProjectProcessor {
    override fun process(project: ExtendedSubmission, context: PersistenceContext) {
        project.addAccessTag(project.accNo)
    }
}
