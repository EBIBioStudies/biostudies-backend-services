package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.persistence.common.request.ListFilter

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
    adminCollections: List<String>,
): ListFilter =
    ListFilter(
        filterUser = user,
        findAnyAccNo = superuser,
        adminCollections = adminCollections,
        accNo = accNo,
        limit = limit,
        offset = offset,
    )
