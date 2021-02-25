package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.submission.exceptions.CollectionInvalidAccessTagException
import ebi.ac.uk.model.constants.SubFields
import java.time.OffsetDateTime

class ParentInfoService(private val queryService: SubmissionMetaQueryService) {
    fun getParentInfo(parentAccNo: String?): ParentInfo = when (parentAccNo) {
        null -> ParentInfo(emptyList(), null, null)
        else -> parentInfo(parentAccNo)
    }

    private fun parentInfo(parentAccNo: String): ParentInfo {
        val project = queryService.getBasicCollection(parentAccNo)
        return ParentInfo(accessTags(parentAccNo), project.releaseTime, project.accNoPattern)
    }

    private fun accessTags(parentAccNo: String): List<String> {
        val accessTags = queryService.getAccessTags(parentAccNo).filterNot { it == SubFields.PUBLIC_ACCESS_TAG.value }
        require(accessTags.contains(parentAccNo)) { throw CollectionInvalidAccessTagException(parentAccNo) }

        return accessTags
    }
}

data class ParentInfo(val accessTags: List<String>, val releaseTime: OffsetDateTime?, val parentTemplate: String?)
