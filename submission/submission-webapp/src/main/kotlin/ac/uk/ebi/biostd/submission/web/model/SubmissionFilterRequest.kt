package ac.uk.ebi.biostd.submission.web.model

import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter

@Suppress("LongParameterList")
class SubmissionFilterRequest(
    val accNo: String? = null,
    val limit: Int = 15,
    val offset: Long = 0,
)

fun SubmissionFilterRequest.asFilter(
    user: String,
    superuser: Boolean,
    adminCollections: List<String>,
): SubmissionListFilter =
    SubmissionListFilter(
        filterUser = user,
        findAnyAccNo = superuser,
        adminCollections = adminCollections,
        accNo = accNo,
        limit = limit,
        offset = offset,
    )
