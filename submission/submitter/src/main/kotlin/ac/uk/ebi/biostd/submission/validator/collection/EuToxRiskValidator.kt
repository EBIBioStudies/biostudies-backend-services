package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.common.properties.ValidatorProperties
import ac.uk.ebi.biostd.persistence.exception.CollectionValidationException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.model.extensions.extension
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

class EuToxRiskValidator(
    private val restTemplate: RestTemplate,
    private val validationProperties: ValidatorProperties
) : CollectionValidator {
    override fun validate(submission: Submission, filesSource: FilesSource) {
        val url = validationProperties.euToxRiskValidationApi
        val headers = HttpHeaders().apply { contentType = APPLICATION_JSON }
        val body = body(submission, filesSource)

        restTemplate
            .postForObject<EuToxRiskValidatorResponse>(url, HttpEntity(body, headers))
            .errors
            .map { it.message }
            .ifNotEmpty { throw CollectionValidationException(it) }
    }

    fun body(submission: Submission, filesSource: FilesSource): FileSystemResource {
        val subFile = submission.allFiles().find { it.extension == "xlsx" }
        requireNotNull(subFile) {
            throw CollectionValidationException(listOf("Excel file is required for Eu-ToxRisk submissions"))
        }

        return FileSystemResource(filesSource.getFile(subFile.path))
    }
}

class EuToxRiskValidatorResponse(val errors: List<EuToxRiskValidatorMessage>)
class EuToxRiskValidatorMessage(val message: String)
