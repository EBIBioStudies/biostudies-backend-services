package ac.uk.ebi.biostd.client.dto

data class ReleaseRequestDto(
    val accNo: String,
    val owner: String,
    val relPath: String,
)
