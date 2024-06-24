package ac.uk.ebi.biostd.client.extensions

import ac.uk.ebi.biostd.client.integration.web.SubmissionResponse
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import reactor.core.publisher.Mono

fun SerializationService.deserializeResponse(
    response: WebClient.ResponseSpec,
    format: SubFormat,
): Mono<SubmissionResponse> {
    fun asSubmission(response: ResponseEntity<String>): SubmissionResponse {
        val submission = deserializeSubmission(response.body!!, format)
        return SubmissionResponse(submission, response.statusCodeValue)
    }
    return response.toEntity<String>().map { asSubmission(it) }
}
