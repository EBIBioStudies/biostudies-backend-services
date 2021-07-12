package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.common.properties.ValidatorProperties
import ac.uk.ebi.biostd.persistence.common.exception.CollectionValidationException
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class EuToxRiskValidatorTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val restTemplate: RestTemplate,
    @MockK private val validationProperties: ValidatorProperties
) {
    private val testUrl = "http://eutoxrisk.org/validator"
    private val textFile = temporaryFolder.createFile("test.txt")
    private val excelFile = temporaryFolder.createFile("test.xlsx")
    private val testInstance = EuToxRiskValidator(restTemplate, validationProperties)

    @BeforeEach
    fun beforeEach() {
        every { validationProperties.euToxRiskValidationApi } returns testUrl
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun validate() {
        val requestSlot = slot<HttpEntity<FileSystemResource>>()
        val submission = basicExtSubmission.copy(
            section = ExtSection(
                type = "Study",
                files = listOf(left(NfsFile("test.xlsx", excelFile)))
            )
        )

        every {
            restTemplate.postForObject<EuToxRiskValidatorResponse>(testUrl, capture(requestSlot))
        } returns EuToxRiskValidatorResponse(listOf())

        testInstance.validate(submission)

        verify(exactly = 1) {
            restTemplate.postForObject(testUrl, requestSlot.captured, EuToxRiskValidatorResponse::class.java)
        }
    }

    @Test
    fun validateWhenNotApplicable() {
        val submission = basicExtSubmission.copy(
            section = ExtSection(
                type = "Study",
                attributes = listOf(ExtAttribute(name = SKIP_VALIDATION_ATTR, value = "UPF12_MITOTOX_1"))
            )
        )

        testInstance.validate(submission)

        verify { restTemplate wasNot called }
    }

    @Test
    fun `validate with errors`() {
        val requestSlot = slot<HttpEntity<FileSystemResource>>()
        val submission = basicExtSubmission.copy(
            section = ExtSection(
                type = "Study",
                files = listOf(left(NfsFile("test.xlsx", excelFile)))
            )
        )

        every {
            restTemplate.postForObject<EuToxRiskValidatorResponse>(testUrl, capture(requestSlot))
        } returns EuToxRiskValidatorResponse(listOf(EuToxRiskValidatorMessage("an error")))

        val error = assertThrows<CollectionValidationException> { testInstance.validate(submission) }
        assertThat(error.message).isEqualTo(
            "The submission doesn't comply with the collection requirements. Errors: [an error]"
        )
        verify(exactly = 1) {
            restTemplate.postForObject(testUrl, requestSlot.captured, EuToxRiskValidatorResponse::class.java)
        }
    }

    @Test
    fun `submission without excel file`() {
        val submission = basicExtSubmission.copy(
            section = ExtSection(
                type = "Study",
                files = listOf(left(NfsFile("test.txt", textFile)))
            )
        )

        val error = assertThrows<CollectionValidationException> { testInstance.validate(submission) }
        assertThat(error.message).isEqualTo(
            "The submission doesn't comply with the collection requirements. Errors: [$EXCEL_FILE_REQUIRED]"
        )
    }
}
