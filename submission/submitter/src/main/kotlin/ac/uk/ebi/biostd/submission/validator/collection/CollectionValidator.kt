package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.persistence.exception.CollectionValidationException
import ebi.ac.uk.extended.model.ExtSubmission

interface CollectionValidator {
    @Throws(CollectionValidationException::class)
    fun validate(submission: ExtSubmission)
}
