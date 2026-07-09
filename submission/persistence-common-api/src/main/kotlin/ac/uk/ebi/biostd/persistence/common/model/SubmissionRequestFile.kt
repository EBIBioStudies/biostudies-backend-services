package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileSourceType

data class SubmissionRequestFile(
    val accNo: String,
    val version: Int,
    val path: String,
    val file: ExtFile,
    val sourceFile: ExtFile,
    val status: RequestFileStatus,
    val sourceType: FileSourceType?,
    val previousSubFile: Boolean = false,
) {
    constructor(
        sub: ExtSubmission,
        file: ExtFile,
        status: RequestFileStatus,
        sourceType: FileSourceType?,
        previousSubFile: Boolean = false,
    ) : this(
        accNo = sub.accNo,
        version = sub.version,
        path = file.filePath,
        file = file,
        status = status,
        sourceFile = file,
        sourceType = sourceType,
        previousSubFile = previousSubFile,
    )
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
