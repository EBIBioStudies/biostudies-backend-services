package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.persistence.common.exception.CollectionValidationException
import ebi.ac.uk.extended.model.ExtSubmission

interface CollectionValidator {
    @Throws(CollectionValidationException::class)
    suspend fun validate(submission: ExtSubmission)
}
