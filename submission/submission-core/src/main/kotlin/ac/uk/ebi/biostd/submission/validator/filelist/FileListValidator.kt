package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.service.FileSourcesRequest
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEmpty
import uk.ac.ebi.biostd.exception.InvalidFileListException
import uk.ac.ebi.biostd.integration.SerializationService
import uk.ac.ebi.biostd.integration.SubFormat
import uk.ac.ebi.biostd.io.sources.FileSourcesList
import uk.ac.ebi.biostd.paths.FolderType
import uk.ac.ebi.biostd.service.PageTabFileReader.getFileListFile
import java.io.InputStream

class FileListValidator(
    private val fileSourcesService: FileSourcesService,
    private val serializationService: SerializationService,
    private val submissionQueryService: SubmissionPersistenceQueryService,
) {
    /**
     * Validates the given file list by locating and deserializing it.
     */
    suspend fun validateFileList(request: FileListValidationRequest) {
        val (accNo, rootPath, fileListName, submitter, onBehalfUser) = request
        val submission = accNo?.let { submissionQueryService.findExtByAccNo(accNo, includeFileListFiles = false) }
        val fileSourcesRequest =
            FileSourcesRequest(
                folderType = FolderType.FTP,
                onBehalfUser = onBehalfUser,
                submitter = submitter,
                files = null,
                rootPath = rootPath,
                previousVersion = submission,
                preferredSources = emptyList(),
            )
        val fileSources = fileSourcesService.submissionSources(fileSourcesRequest)
        validateFileList(fileListName, fileSources)
    }

    private suspend fun validateFileList(
        fileListName: String,
        fileSources: FileSourcesList,
    ) {
        val fileListFile = getFileListFile(fileListName, fileSources)
        val format = SubFormat.fromFile(fileListFile)
        fileListFile.inputStream().use { validateFiles(fileListName, it, format) }
    }

    private suspend fun validateFiles(
        name: String,
        stream: InputStream,
        format: SubFormat,
    ) {
        serializationService
            .deserializeFileListAsFlow(stream, format)
            .onEmpty { throw InvalidFileListException.emptyFileList(name) }
            .collect()
    }
}

data class FileListValidationRequest(
    val accNo: String?,
    val rootPath: String?,
    val fileListName: String,
    val submitter: SecurityUser,
    val onBehalfUser: SecurityUser?,
)
