package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.persistence.common.exception.CollectionValidationException
import uk.ac.ebi.biostd.extended.model.ExtSubmission

interface CollectionValidator {
    @Throws(CollectionValidationException::class)
    suspend fun validate(submission: ExtSubmission)
}
