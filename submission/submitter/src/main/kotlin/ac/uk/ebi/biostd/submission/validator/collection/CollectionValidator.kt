package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.persistence.exception.CollectionValidationException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission

interface CollectionValidator {
    @Throws(CollectionValidationException::class)
    fun validate(submission: Submission, filesSource: FilesSource)
}

