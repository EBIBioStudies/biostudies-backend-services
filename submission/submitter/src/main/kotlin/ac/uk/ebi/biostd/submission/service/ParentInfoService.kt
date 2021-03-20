package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.exception.CollectionValidationException
import ac.uk.ebi.biostd.submission.exceptions.CollectionInvalidAccessTagException
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ebi.ac.uk.base.ifFalse
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.isCollection
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import org.springframework.beans.factory.BeanFactory
import java.time.OffsetDateTime

class ParentInfoService(
    private val beanFactory: BeanFactory,
    private val queryService: SubmissionMetaQueryService
) {
    fun getParentInfo(parentAccNo: String?): ParentInfo = when (parentAccNo) {
        null -> ParentInfo(emptyList(), null, null)
        else -> parentInfo(parentAccNo)
    }

    private fun parentInfo(parentAccNo: String): ParentInfo {
        val parentInfo = collectionInfo(parentAccNo)

        return ParentInfo(accessTags(parentAccNo), parentInfo.releaseTime, parentInfo.accNoPattern)
    }

    private fun accessTags(parentAccNo: String): List<String> {
        val accessTags = queryService.getAccessTags(parentAccNo).filterNot { it == PUBLIC_ACCESS_TAG.value }
        require(accessTags.contains(parentAccNo)) { throw CollectionInvalidAccessTagException(parentAccNo) }

        return accessTags
    }

    fun executeCollectionValidators(submission: ExtSubmission) = submission.isCollection.ifFalse {
        submission
            .collections
            .mapNotNull { collectionInfo(it.accNo).validator }
            .forEach { validate(it, submission) }
    }

    private fun collectionInfo(accNo: String) = queryService.getBasicCollection(accNo)

    @Throws(CollectionValidationException::class)
    private fun validate(validator: String, submission: ExtSubmission) = loadValidator(validator).validate(submission)

    private fun loadValidator(name: String) = beanFactory.getBean(name, CollectionValidator::class.java)
}

data class ParentInfo(val accessTags: List<String>, val releaseTime: OffsetDateTime?, val parentTemplate: String?)
