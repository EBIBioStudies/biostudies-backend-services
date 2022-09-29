package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.Permissions
import java.io.File

internal interface FilesService {
    fun persistSubmissionFile(request: FilePersistenceRequest): ExtFile

    fun cleanSubmissionFile(file: ExtFile)
}

interface FilePersistenceRequest

data class FireFilePersistenceRequest(
    val accNo: String,
    val version: Int,
    val relPath: String,
    val file: ExtFile,
) : FilePersistenceRequest

data class NfsFilePersistenceRequest(
    val file: NfsFile,
    val subFolder: File,
    val targetFolder: File,
    val permissions: Permissions
) : FilePersistenceRequest

