package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmissionOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import org.springframework.web.client.RestTemplate

internal class SubmissionClientImpl(
    private val serializationService: SerializationService,
    private val restTemplate: RestTemplate
) : SubmissionClient,
    FilesOperations by UserFilesClient(restTemplate),
    GroupFilesOperations by GroupFilesClient(restTemplate),
    SubmissionOperations by SubmissionClient(restTemplate, serializationService),
    MultipartSubmissionOperations by MultiPartSubmissionClient(restTemplate, serializationService)
