package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.persistence.common.exception.CollectionValidationException
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ebi.ac.uk.base.ifFalse
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.isCollection
import org.springframework.beans.factory.BeanFactory

class CollectionValidationService(
    private val beanFactory: BeanFactory,
    private val queryService: SubmissionMetaQueryService
) {
    fun executeCollectionValidators(submission: ExtSubmission) = submission.isCollection.ifFalse {
        submission
            .collections
            .mapNotNull { loadCollection(it.accNo).validator }
            .forEach { validate(it, submission) }
    }

    private fun loadCollection(accNo: String) = queryService.getBasicCollection(accNo)

    @Throws(CollectionValidationException::class)
    private fun validate(validator: String, submission: ExtSubmission) = loadValidator(validator).validate(submission)

    private fun loadValidator(name: String) = beanFactory.getBean(name, CollectionValidator::class.java)
}
