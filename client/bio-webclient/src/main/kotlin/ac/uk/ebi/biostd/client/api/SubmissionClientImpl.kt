package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.MultipartSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.MultipartAsyncSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.PermissionOperations
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

internal class SubmissionClientImpl(
    private val template: RestTemplate,
    private val serializationService: SerializationService,
    private val extSerializationService: ExtSerializationService
) : SubmissionClient,
    FilesOperations by UserFilesClient(template),
    GroupFilesOperations by GroupFilesClient(template),
    SubmissionOperations by SubmissionClient(template, serializationService),
    MultipartSubmissionOperations by MultiPartSubmissionClient(template, serializationService),
    MultipartAsyncSubmissionOperations by MultiPartAsyncSubmissionClient(template, serializationService),
    GeneralOperations by CommonOperationsClient(template),
    DraftSubmissionOperations by SubmissionDraftClient(template),
    ExtSubmissionOperations by ExtSubmissionClient(template, extSerializationService),
    PermissionOperations by PermissionOperationsClient(template)
