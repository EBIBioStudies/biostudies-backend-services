package ac.uk.ebi.biostd.persistence.common.model

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileSourceType

/**
 * Represents a file associated with a submission request.
 *
 * @property cleanUpRecord flag that marks request-file records representing persisted submission files considered
 * during clean-up. It is part of the persistence identity to avoid collisions for the same accNo/version/path.
 */
data class SubmissionRequestFile(
    val accNo: String,
    val version: Int,
    val path: String,
    val file: ExtFile,
    val sourceFile: ExtFile,
    val status: RequestFileStatus,
    val sourceType: FileSourceType?,
    val cleanUpRecord: Boolean = false,
) {
    constructor(
        sub: ExtSubmission,
        file: ExtFile,
        status: RequestFileStatus,
        sourceType: FileSourceType?,
        sourceFile: ExtFile = file,
        cleanUpRecord: Boolean = false,
    ) : this(
        accNo = sub.accNo,
        version = sub.version,
        path = file.filePath,
        file = file,
        status = status,
        sourceFile = sourceFile,
        sourceType = sourceType,
        cleanUpRecord = cleanUpRecord,
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
