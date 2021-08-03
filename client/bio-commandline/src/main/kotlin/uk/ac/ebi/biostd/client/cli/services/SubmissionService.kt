package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.model.Submission
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.extended.serialization.integration.ExtSerializationConfig.extSerializationService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File

/**
 * In charge of perform submission command line operations.
 */
@Suppress("TooManyFunctions")
internal class SubmissionService {
    fun submit(request: SubmissionRequest): Submission = performRequest { submitRequest(request) }

    fun submitAsync(request: SubmissionRequest) = performRequest { submitAsyncRequest(request) }

    fun delete(request: DeletionRequest) = performRequest { deleteRequest(request) }

    fun migrate(request: MigrationRequest) = performRequest { migrateRequest(request) }

    private fun submitRequest(request: SubmissionRequest): Submission =
        bioWebClient(request.server, request.user, request.password).submitSingle(request.file, request.attached).body

    private fun submitAsyncRequest(request: SubmissionRequest) =
        bioWebClient(request.server, request.user, request.password).asyncSubmitSingle(request.file, request.attached)

    private fun deleteRequest(request: DeletionRequest) =
        bioWebClient(request.server, request.user, request.password).deleteSubmissions(request.accNoList)

    private fun migrateRequest(request: MigrationRequest) {
        val sourceClient = bioWebClient(request.source, request.sourceUser, request.sourcePassword)
        val targetClient = bioWebClient(request.target, request.targetUser, request.targetPassword)
        val extSerializer = extSerializationService(request.source)
        val source = sourceClient.getExtByAccNo(request.accNo)
        val migrated = migratedSubmissions(source, request.targetOwner)
        val fileLists = source.allFileList.map { asTempFile(it, sourceClient, extSerializer) }
        targetClient.submitExt(migrated, fileLists)
    }

    private fun asTempFile(fileList: ExtFileList, client: BioWebClient, extSerializer: ExtSerializationService): File {
        val files = client.getReferencedFiles(fileList.filesUrl!!)
        // TODO using default UNIX temp folder. Should this be configurable?
        val file = File("/tmp", fileList.fileName)
        file.writeText(extSerializer.serialize(files))

        return file
    }

    private fun migratedSubmissions(submission: ExtSubmission, targetOwner: String?) =
        if (targetOwner == null) submission else submission.copy(owner = targetOwner)
}
