package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.persistence.common.exception.CollectionValidationException
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ebi.ac.uk.extended.model.ExtSubmission

class TestCollectionValidator : CollectionValidator {
    var validated = false

    override suspend fun validate(submission: ExtSubmission) {
        validated = true
    }
}

class FailCollectionValidator : CollectionValidator {
    override suspend fun validate(submission: ExtSubmission): Unit = throw CollectionValidationException(listOf("Testing failure"))
}
