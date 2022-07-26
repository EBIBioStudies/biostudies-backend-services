package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.service.PageTabFileReader.getFileListFile
import ac.uk.ebi.biostd.submission.service.FileSourcesRequest
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.extensions.md5
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.util.collections.ifNotEmpty
import java.io.InputStream

class FileListValidator(
    private val fileSourcesService: FileSourcesService,
    private val serializationService: SerializationService,
    private val submissionQueryService: SubmissionPersistenceQueryService,
) {
    /**
     * Validates the given file list by deserializing it and checking each file presence by generating a file source
     * with the given parameters. Note that in case of missing files only first 1000 are reported.
     */
    fun validateFileList(request: FileListValidationRequest) {
        val (accNo, rootPath, fileListName, submitter, onBehalfUser) = request
        val submission = accNo?.let { submissionQueryService.findExtByAccNo(accNo, includeFileListFiles = false) }
        val fileSourcesRequest = FileSourcesRequest(
            onBehalfUser = onBehalfUser,
            submitter = submitter,
            files = null,
            rootPath = rootPath,
            submission = submission,
            preferredSources = emptyList()
        )
        val fileSources = fileSourcesService.submissionSources(fileSourcesRequest)
        val fileListFile = getFileListFile(fileListName, fileSources)

        fileListFile.inputStream().use { validateFileList(it, SubFormat.fromFile(fileListFile), fileSources) }
    }

    private fun validateFileList(stream: InputStream, format: SubFormat, filesSource: FileSourcesList) {
        serializationService
            .deserializeFileList(stream, format)
            .filter { filesSource.getExtFile(it.path, it.md5) == null }
            .take(fileListLimit)
            .toList()
            .ifNotEmpty { throw FilesProcessingException(it.map(BioFile::path), filesSource) }
    }

    companion object {
        private const val fileListLimit = 1000
    }
}

data class FileListValidationRequest(
    val accNo: String?,
    val rootPath: String?,
    val fileListName: String,
    val submitter: SecurityUser,
    val onBehalfUser: SecurityUser?,
)
