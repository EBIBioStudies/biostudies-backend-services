package uk.ac.ebi.scheduler.releaser.model

import ac.uk.ebi.biostd.client.dto.ReleaseRequestDto

data class ReleaseData(
    val accNo: String,
    val owner: String,
    val relPath: String
) {
    fun asReleaseDto() = ReleaseRequestDto(accNo, owner, relPath)
}
