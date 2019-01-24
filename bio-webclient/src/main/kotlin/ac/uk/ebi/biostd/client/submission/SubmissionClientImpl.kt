package ac.uk.ebi.biostd.client.submission

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionClient
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import org.springframework.web.client.RestTemplate

internal class SubmissionClientImpl(
    private val serializationService: SerializationService,
    private val template: RestTemplate
) : SubmissionClient,
    FilesOperations by UserFilesClient(template),
    SubmissionOperations by SubmissionClient(template, serializationService),
    GroupFilesOperations by GroupFilesClient(template)
