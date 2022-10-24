package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ac.uk.ebi.biostd.client.integration.web.MultipartAsyncSubmitOperations
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmitOperations
import ac.uk.ebi.biostd.client.integration.web.PermissionOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.SubmitClient
import ac.uk.ebi.biostd.client.integration.web.SubmitOperations
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

internal class SubmitClientImpl(
    private val template: RestTemplate,
    private val serializationService: SerializationService,
    private val extSerializationService: ExtSerializationService,
) : SubmitClient,
    FilesOperations by UserFilesClient(template),
    GroupFilesOperations by GroupFilesClient(template),
    SubmissionOperations by SubmissionClient(template),
    SubmitOperations by SubmitClient(template, serializationService),
    MultipartSubmitOperations by MultiPartSubmitClient(template, serializationService),
    MultipartAsyncSubmitOperations by MultiPartAsyncSubmitClient(template, serializationService),
    GeneralOperations by CommonOperationsClient(template),
    DraftSubmissionOperations by SubmissionDraftClient(template),
    ExtSubmissionOperations by ExtSubmissionClient(template, extSerializationService),
    PermissionOperations by PermissionOperationsClient(template)
