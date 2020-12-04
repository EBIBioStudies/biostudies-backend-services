package ac.uk.ebi.biostd.submission.web.model

class ExtPageRequest(
    val fromRTime: String? = null,
    val toRTime: String? = null,
    val released: Boolean? = null,
    val offset: Long,
    val limit: Int
)
