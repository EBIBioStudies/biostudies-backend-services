package ac.uk.ebi.biostd.client.extensions

import ac.uk.ebi.biostd.client.integration.web.SubmissionResponse
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec

fun RequestHeadersSpec<*>.submitBlocking(
    serializationService: SerializationService,
    format: SubFormat,
): SubmissionResponse {
    return retrieve()
        .toEntity(String::class.java)
        .map { body ->
            val submission = serializationService.deserializeSubmission(body.body!!, format)
            SubmissionResponse(submission, body.statusCodeValue)
        }
        .block()!!
}
