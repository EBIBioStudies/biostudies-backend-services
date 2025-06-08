package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission

data class SubmissionRequestFile(
    val accNo: String,
    val version: Int,
    val path: String,
    val file: ExtFile,
    val status: RequestFileStatus,
    val previousSubFile: Boolean = false,
) {
    constructor(
        sub: ExtSubmission,
        file: ExtFile,
        status: RequestFileStatus,
        previousSubFile: Boolean = false,
    ) : this(sub.accNo, sub.version, file.filePath, file, status, previousSubFile)
}

enum class RequestFileStatus {
    INDEXED,
    LOADED,
    COPIED,
    CLEANED,
    RELEASED,
    UNRELEASED,
    CONFLICTING,
    CONFLICTING_PAGE_TAB,
    DEPRECATED,
    DEPRECATED_PAGE_TAB,
    REUSED,
}
