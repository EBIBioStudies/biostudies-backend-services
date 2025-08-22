package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ac.uk.ebi.biostd.client.integration.web.MultipartAsyncSubmitOperations
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmitOperations
import ac.uk.ebi.biostd.client.integration.web.PermissionOperations
import ac.uk.ebi.biostd.client.integration.web.PostProcessOperations
import ac.uk.ebi.biostd.client.integration.web.StatsOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionRequestOperations
import ac.uk.ebi.biostd.client.integration.web.SubmitClient
import ac.uk.ebi.biostd.client.integration.web.SubmitOperations
import ac.uk.ebi.biostd.client.integration.web.UserOperations
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

internal class SubmitClientImpl(
    private val client: WebClient,
    private val serializationService: SerializationService,
    private val extSerializationService: ExtSerializationService,
) : SubmitClient,
    FilesOperations by UserFilesClient(client),
    UserOperations by UserOperationsClient(client),
    GroupFilesOperations by GroupFilesClient(client),
    SubmissionOperations by SubmissionClient(client),
    SubmitOperations by SubmitClient(client, serializationService),
    MultipartSubmitOperations by MultiPartSubmitClient(client, serializationService),
    MultipartAsyncSubmitOperations by MultiPartAsyncSubmitClient(client, serializationService),
    GeneralOperations by CommonOperationsClient(client),
    DraftSubmissionOperations by SubmissionDraftClient(client),
    ExtSubmissionOperations by ExtSubmissionClient(client, extSerializationService),
    PermissionOperations by PermissionOperationsClient(client),
    StatsOperations by StatsClient(client),
    SubmissionRequestOperations by SubmissionRequestClient(client),
    PostProcessOperations by PostProcessOperationsClient(client)
