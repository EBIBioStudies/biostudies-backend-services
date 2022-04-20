package uk.ac.ebi.biostd.client.cli.services

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.io.FileUtils.writeContent
import ebi.ac.uk.model.Submission
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig.extSerializationService
import java.io.File

/**
 * In charge of performing submission command line operations.
 */
@Suppress("TooManyFunctions")
internal class SubmissionService {
    private val extSerializer = extSerializationService()

    fun submit(request: SubmissionRequest): Submission = performRequest { submitRequest(request) }

    fun submitAsync(request: SubmissionRequest) = performRequest { submitAsyncRequest(request) }

    fun delete(request: DeletionRequest) = performRequest { deleteRequest(request) }

    fun migrate(request: MigrationRequest) = performRequest { migrateRequest(request) }

    private fun submitRequest(request: SubmissionRequest): Submission =
        bioWebClient(request.server, request.user, request.password, request.onBehalf)
            .submitSingle(request.file, request.attached, fileMode = request.fileMode)
            .body

    private fun submitAsyncRequest(request: SubmissionRequest) =
        bioWebClient(request.server, request.user, request.password, request.onBehalf)
            .asyncSubmitSingle(request.file, request.attached, fileMode = request.fileMode)

    private fun deleteRequest(request: DeletionRequest) =
        bioWebClient(request.server, request.user, request.password).deleteSubmissions(request.accNoList)

    private fun migrateRequest(request: MigrationRequest) {
        val sourceClient = bioWebClient(request.source, request.sourceUser, request.sourcePassword)
        val targetClient = bioWebClient(request.target, request.targetUser, request.targetPassword)
        val source = sourceClient.getExtByAccNo(request.accNo)
        val sub = migratedSubmission(source, request.targetOwner)
        val fileLists = source.allFileList
            .map { File(request.tempFolder, it.filePath) to sourceClient.getReferencedFiles(it.filesUrl!!) }
            .onEach { (file, files) -> writeContent(file, extSerializer.serialize(files)) }
            .map { it.first }

        if (request.async) targetClient.submitExtAsync(sub, fileLists, request.fileMode)
        else targetClient.submitExt(sub, fileLists, request.fileMode)
    }

    private fun migratedSubmission(submission: ExtSubmission, targetOwner: String?) =
        if (targetOwner == null) submission else submission.copy(owner = targetOwner)
}
