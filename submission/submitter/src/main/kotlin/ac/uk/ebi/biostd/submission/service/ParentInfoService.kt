package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ebi.ac.uk.model.constants.SubFields
import java.time.OffsetDateTime

class ParentInfoService(private val queryService: SubmissionQueryService) {
    fun getParentInfo(parentAccNo: String?): ParentInfo = when (parentAccNo) {
        null -> ParentInfo(emptyList(), null, null)
        else -> parentInfo(parentAccNo)
    }

    private fun parentInfo(parentAccNo: String): ParentInfo {
        require(queryService.existByAccNo(parentAccNo)) { "The project '$parentAccNo' doesn't exist" }
        return ParentInfo(
            queryService.getAccessTags(parentAccNo).filterNot { it == SubFields.PUBLIC_ACCESS_TAG.value },
            queryService.getReleaseTime(parentAccNo),
            queryService.getParentAccPattern(parentAccNo))
    }
}

data class ParentInfo(val accessTags: List<String>, val releaseTime: OffsetDateTime?, val parentTemplate: String?)
