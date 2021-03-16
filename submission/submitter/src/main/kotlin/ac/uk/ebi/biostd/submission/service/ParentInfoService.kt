package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.exception.CollectionValidationException
import ac.uk.ebi.biostd.submission.exceptions.CollectionInvalidAccessTagException
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidator
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.beans.factory.BeanFactory
import java.time.OffsetDateTime

class ParentInfoService(
    private val beanFactory: BeanFactory,
    private val queryService: SubmissionMetaQueryService
) {
    fun getParentInfo(submission: Submission, source: FilesSource): ParentInfo = when (submission.attachTo) {
        null -> ParentInfo(emptyList(), null, null)
        else -> parentInfo(submission, source)
    }

    private fun parentInfo(submission: Submission, source: FilesSource): ParentInfo {
        val parentAccNo = submission.attachTo!!
        val (_, accNoPattern, validator, releaseTime) = queryService.getBasicCollection(parentAccNo)
        validator?.let { validateSubmission(submission, source, it) }

        return ParentInfo(accessTags(parentAccNo), releaseTime, accNoPattern)
    }

    private fun accessTags(parentAccNo: String): List<String> {
        val accessTags = queryService.getAccessTags(parentAccNo).filterNot { it == PUBLIC_ACCESS_TAG.value }
        require(accessTags.contains(parentAccNo)) { throw CollectionInvalidAccessTagException(parentAccNo) }

        return accessTags
    }

    private fun validateSubmission(submission: Submission, source: FilesSource, validator: String) =
        beanFactory
            .getBean(validator, CollectionValidator::class.java)
            .validate(submission, source)
}

data class ParentInfo(val accessTags: List<String>, val releaseTime: OffsetDateTime?, val parentTemplate: String?)
