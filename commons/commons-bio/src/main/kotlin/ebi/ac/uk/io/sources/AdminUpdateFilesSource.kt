package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import java.io.File

/**
 * Source that allows the submitter to bypass the file path check. It's intended to be used ONLY by admins to update
 * submissions made before the system started enforcing file path S3 compliance so existing data isn't affected.
 */
object AdminUpdateFilesSource : FilesSource {
    override val description: String
        get() = "Admin Metadata Update"

    override suspend fun getExtFile(
        path: String,
        type: String,
        attributes: List<ExtAttribute>,
    ): ExtFile? = null

    override suspend fun getFileList(path: String): File? = null
}
