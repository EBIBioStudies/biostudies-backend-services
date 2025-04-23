package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.common.properties.ValidatorProperties
import ac.uk.ebi.biostd.persistence.common.exception.CollectionValidationException
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.RequestFile
import ebi.ac.uk.extended.model.allSectionsFiles
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.io.File

internal const val EXCEL_FILE_REQUIRED = "Excel file is required for Eu-ToxRisk submissions"
internal const val SKIP_VALIDATION_ATTR = "QMRF-ID"

class EuToxRiskValidator(
    private val client: WebClient,
    private val validationProperties: ValidatorProperties,
    private val fireClient: FireClient,
) : CollectionValidator {
    override suspend fun validate(submission: ExtSubmission) {
        if (submission.section.attributes.none { it.name == SKIP_VALIDATION_ATTR }) {
            validateSubmission(validationProperties.euToxRiskValidationApi, submission)
        }
    }

    private suspend fun validateSubmission(
        url: String,
        submission: ExtSubmission,
    ) {
        client
            .postForObject<EuToxRiskValidatorResponse>(url, RequestParams(jsonHeaders(), body(submission)))
            .errors
            .map { it.message }
            .ifNotEmpty { throw CollectionValidationException(it) }
    }

    private fun jsonHeaders() = HttpHeaders().apply { contentType = APPLICATION_JSON }

    private suspend fun body(submission: ExtSubmission): FileSystemResource {
        val subFile =
            submission
                .allSectionsFiles
                .find { it.fileName.endsWith("xlsx") }
                ?: throw CollectionValidationException(listOf(EXCEL_FILE_REQUIRED))
        return FileSystemResource(asFile(subFile))
    }

    private suspend fun asFile(file: ExtFile): File =
        when (file) {
            is FireFile -> fireClient.downloadByPath(file.filePath)!!
            is NfsFile -> file.file
            is RequestFile -> error("Can not obtain File instance from RequestFile ${file.filePath}")
        }
}

class EuToxRiskValidatorResponse(
    val errors: List<EuToxRiskValidatorMessage>,
)

class EuToxRiskValidatorMessage(
    val message: String,
)
