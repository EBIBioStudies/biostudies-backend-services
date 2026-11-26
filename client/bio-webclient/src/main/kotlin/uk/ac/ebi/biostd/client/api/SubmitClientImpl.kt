package uk.ac.ebi.biostd.client.api

import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.biostd.client.integration.web.DraftSubmissionOperations
import uk.ac.ebi.biostd.client.integration.web.ExtSubmissionOperations
import uk.ac.ebi.biostd.client.integration.web.FilesOperations
import uk.ac.ebi.biostd.client.integration.web.GeneralOperations
import uk.ac.ebi.biostd.client.integration.web.GroupFilesOperations
import uk.ac.ebi.biostd.client.integration.web.MultipartSubmitOperations
import uk.ac.ebi.biostd.client.integration.web.PermissionOperations
import uk.ac.ebi.biostd.client.integration.web.PostProcessOperations
import uk.ac.ebi.biostd.client.integration.web.StatsOperations
import uk.ac.ebi.biostd.client.integration.web.SubmissionOperations
import uk.ac.ebi.biostd.client.integration.web.SubmissionRequestOperations
import uk.ac.ebi.biostd.client.integration.web.SubmitClient
import uk.ac.ebi.biostd.client.integration.web.SubmitOperations
import uk.ac.ebi.biostd.client.integration.web.UserOperations
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.biostd.integration.SerializationService

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
    GeneralOperations by CommonOperationsClient(client),
    DraftSubmissionOperations by SubmissionDraftClient(client),
    ExtSubmissionOperations by ExtSubmissionClient(client, extSerializationService),
    PermissionOperations by PermissionOperationsClient(client),
    StatsOperations by StatsClient(client),
    SubmissionRequestOperations by SubmissionRequestClient(client),
    PostProcessOperations by PostProcessOperationsClient(client)
