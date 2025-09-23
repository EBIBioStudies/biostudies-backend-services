package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import java.net.URLDecoder
import java.time.OffsetDateTime

@Suppress("LongParameterList")
class SubmissionFilterRequest(
    val accNo: String? = null,
    val version: Long? = null,
    val type: String? = null,
    val rTimeFrom: String? = null,
    val rTimeTo: String? = null,
    val keywords: String? = null,
    val released: Boolean? = null,
    val limit: Int = 15,
    val offset: Long = 0,
)

fun SubmissionFilterRequest.asFilter(
    user: String,
    superuser: Boolean,
): SubmissionListFilter =
    SubmissionListFilter(
        filterUser = user,
        findAnyAccNo = superuser,
        accNo = accNo,
        version = version,
        type = type,
        rTimeFrom = rTimeFrom?.let { OffsetDateTime.parse(it) },
        rTimeTo = rTimeTo?.let { OffsetDateTime.parse(it) },
        keywords = keywords?.let { URLDecoder.decode(keywords, Charsets.UTF_8.name()) },
        released = released,
        limit = limit,
        offset = offset,
    )
