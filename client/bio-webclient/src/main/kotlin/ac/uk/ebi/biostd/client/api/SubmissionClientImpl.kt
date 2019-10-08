package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.ProjectOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ac.uk.ebi.biostd.integration.SerializationService
import org.springframework.web.client.RestTemplate

internal class SubmissionClientImpl(
    private val serializationService: SerializationService,
    private val template: RestTemplate
) : SubmissionClient,
    FilesOperations by UserFilesClient(template),
    GroupFilesOperations by GroupFilesClient(template),
    SubmissionOperations by SubmissionClient(template, serializationService),
    ProjectOperations by ProjectClient(template, serializationService),
    MultipartSubmissionOperations by MultiPartSubmissionClient(template, serializationService),
    GeneralOperations by CommonOperationsClient(template),
    DraftSubmissionOperations by SubmissionDraftClient(template)
