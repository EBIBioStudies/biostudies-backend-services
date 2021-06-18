package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.common.properties.ValidatorProperties
import ac.uk.ebi.biostd.persistence.exception.CollectionValidationException
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.allFiles
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

internal const val EXCEL_FILE_REQUIRED = "Excel file is required for Eu-ToxRisk submissions"
internal const val SKIP_VALIDATION_ATTR = "QMRF-ID"

class EuToxRiskValidator(
    private val restTemplate: RestTemplate,
    private val validationProperties: ValidatorProperties
) : CollectionValidator {
    override fun validate(submission: ExtSubmission) {
        if (submission.section.attributes.none { it.name == SKIP_VALIDATION_ATTR })
            validateSubmission(validationProperties.euToxRiskValidationApi, submission)
    }

    private fun validateSubmission(url: String, submission: ExtSubmission) {
        restTemplate
            .postForObject<EuToxRiskValidatorResponse>(url, HttpEntity(body(submission), jsonHeaders()))
            .errors
            .map { it.message }
            .ifNotEmpty { throw CollectionValidationException(it) }
    }

    private fun jsonHeaders() = HttpHeaders().apply { contentType = APPLICATION_JSON }

    fun body(submission: ExtSubmission): FileSystemResource {
        val subFile = submission
            .allFiles
            .filterIsInstance<NfsFile>()
            .find { it.file.extension == "xlsx" }
            ?: throw CollectionValidationException(listOf(EXCEL_FILE_REQUIRED))

        return FileSystemResource(subFile.file)
    }
}

class EuToxRiskValidatorResponse(val errors: List<EuToxRiskValidatorMessage>)
class EuToxRiskValidatorMessage(val message: String)
