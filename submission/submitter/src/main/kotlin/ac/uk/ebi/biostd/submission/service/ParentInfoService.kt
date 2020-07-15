package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.ProjectInvalidAccessTagException
import ebi.ac.uk.model.constants.SubFields
import java.time.OffsetDateTime

class ParentInfoService(private val queryService: SubmissionQueryService) {
    fun getParentInfo(parentAccNo: String?): ParentInfo = when (parentAccNo) {
        null -> ParentInfo(emptyList(), null, null)
        else -> parentInfo(parentAccNo)
    }

    private fun parentInfo(parentAccNo: String): ParentInfo {
        require(queryService.existByAccNo(parentAccNo)) { throw ProjectNotFoundException(parentAccNo) }

        val accessTags = queryService.getAccessTags(parentAccNo).filterNot { it == SubFields.PUBLIC_ACCESS_TAG.value }
        require(accessTags.contains(parentAccNo)) { throw ProjectInvalidAccessTagException(parentAccNo) }

        return ParentInfo(
            accessTags,
            queryService.getReleaseTime(parentAccNo),
            queryService.getParentAccPattern(parentAccNo))
    }
}

data class ParentInfo(val accessTags: List<String>, val releaseTime: OffsetDateTime?, val parentTemplate: String?)
