package ac.uk.ebi.biostd.submission.service

import ebi.ac.uk.persistence.PersistenceContext
import java.time.OffsetDateTime

class ParentInfoService(private val ctx: PersistenceContext) {
    fun getParentInfo(parentAccNo: String?): ParentInfo = when (parentAccNo) {
        null -> ParentInfo(emptyList(), null)
        else -> {
            require(ctx.existByAccNo(parentAccNo)) { "Could not find a project register with accNo $parentAccNo" }
            ParentInfo(ctx.getAccessTags(parentAccNo).filterNot { it == "Public" }, ctx.getReleaseTime(parentAccNo))
        }
    }
}

data class ParentInfo(val accessTags: List<String>, val releaseTime: OffsetDateTime?)
