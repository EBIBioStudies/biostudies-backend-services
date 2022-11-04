package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtFile

data class SubmissionRequestFile(
    val accNo: String,
    val version: Int,
    val index: Int,
    val path: String,
    val file: ExtFile,
)
