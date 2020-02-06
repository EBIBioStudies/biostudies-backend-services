package ac.uk.ebi.biostd.submission.service

import ebi.ac.uk.persistence.PersistenceContext
import java.time.OffsetDateTime

class ParentInfoService(private val ctx: PersistenceContext) {

    fun getParentInfo(attachTo: String?): ParentInfo {
        return when (attachTo) {
            null -> ParentInfo(emptyList(), null)
            else -> {
                require(ctx.existByAccNo(attachTo)) { "Could not find a project register with accNo $attachTo" }
                ParentInfo(ctx.getAccessTags(attachTo).filterNot { it == "Public" }, ctx.getReleaseTime(attachTo))
            }
        }
    }
}

data class ParentInfo(val accessTags: List<String>, val releaseTime: OffsetDateTime?)
