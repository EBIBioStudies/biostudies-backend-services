package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission

data class SubmissionRequestFile(
    val accNo: String,
    val version: Int,
    val index: Int,
    val path: String,
    val file: ExtFile,
    val status: RequestFileStatus,
) {
    constructor(
        sub: ExtSubmission,
        index: Int,
        file: ExtFile,
        status: RequestFileStatus,
    ) : this(sub.accNo, sub.version, index, file.filePath, file, status)
}

enum class RequestFileStatus {
    INDEXED,
    LOADED,
    COPIED,
    RELEASED,
    CONFLICTING,
    DEPRECATED,
}
