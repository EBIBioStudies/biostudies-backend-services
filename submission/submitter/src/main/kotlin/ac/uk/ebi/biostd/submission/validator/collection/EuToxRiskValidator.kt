package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.common.properties.ValidatorProperties
import ac.uk.ebi.biostd.persistence.exception.CollectionValidationException
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.model.extensions.allFiles
import ebi.ac.uk.model.extensions.extension
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

internal const val EXCEL_FILE_REQUIRED = "Excel file is required for Eu-ToxRisk submissions"

class EuToxRiskValidator(
    private val restTemplate: RestTemplate,
    private val validationProperties: ValidatorProperties
) : CollectionValidator {
    override fun validate(submission: ExtSubmission) {
        val url = validationProperties.euToxRiskValidationApi
        val headers = HttpHeaders().apply { contentType = APPLICATION_JSON }
        val body = body(submission)

        restTemplate
            .postForObject<EuToxRiskValidatorResponse>(url, HttpEntity(body, headers))
            .errors
            .map { it.message }
            .ifNotEmpty { throw CollectionValidationException(it) }
    }

    fun body(submission: ExtSubmission): FileSystemResource {
        val subFile = submission.allFiles.find { it.file.extension == "xlsx" }
        requireNotNull(subFile) {
            throw CollectionValidationException(listOf(EXCEL_FILE_REQUIRED))
        }

        return FileSystemResource(subFile.file)
    }
}

class EuToxRiskValidatorResponse(val errors: List<EuToxRiskValidatorMessage>)
class EuToxRiskValidatorMessage(val message: String)
