package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.InvalidSectionAccNoException
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.allLibraryFileSections
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.collections.ifNotEmpty

class LibraryFileSectionValidator : SubmissionValidator {
    // TODO discuss whether this should be replaced by a generated acc no
    override fun validate(submission: ExtendedSubmission, context: PersistenceContext) {
        submission
            .allLibraryFileSections()
            .filter { it.accNo.isNullOrBlank() }
            .ifNotEmpty { throw InvalidSectionAccNoException(it) }
    }
}
