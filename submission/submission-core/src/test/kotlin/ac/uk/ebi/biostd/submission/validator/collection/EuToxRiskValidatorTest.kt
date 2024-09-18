package ac.uk.ebi.biostd.submission.validator.collection

import ac.uk.ebi.biostd.common.properties.ValidatorProperties
import ac.uk.ebi.biostd.persistence.common.exception.CollectionValidationException
import ebi.ac.uk.base.Either.Companion.left
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.createNfsFile
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
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.util.function.Consumer

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class EuToxRiskValidatorTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val client: WebClient,
    @MockK private val validationProperties: ValidatorProperties,
    @MockK private val fireClient: FireClient,
    @MockK private val requestSpec: RequestBodySpec,
) {
    private val testUrl = "http://eutoxrisk.org/validator"
    private val textFile = temporaryFolder.createFile("test.txt")
    private val excelFile = temporaryFolder.createFile("test.xlsx")
    private val testInstance = EuToxRiskValidator(client, validationProperties, fireClient)

    @BeforeEach
    fun beforeEach() {
        every { validationProperties.euToxRiskValidationApi } returns testUrl
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun validate() =
        runTest {
            val bodySlot = slot<FileSystemResource>()
            val headersSlot = slot<Consumer<HttpHeaders>>()
            val submission =
                basicExtSubmission.copy(
                    section =
                        ExtSection(
                            type = "Study",
                            files =
                                listOf(
                                    left(createNfsFile("folder/test.xlsx", "Files/folder/test.xlsx", excelFile)),
                                ),
                        ),
                )

            every { client.post().uri(testUrl) } returns requestSpec
            every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
            every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
            every {
                requestSpec.retrieve().bodyToMono(EuToxRiskValidatorResponse::class.java).block()
            } returns EuToxRiskValidatorResponse(listOf())

            testInstance.validate(submission)

            val body = bodySlot.captured
            val headers = headersSlot.captured
            headers.andThen {
                assertThat(it[HttpHeaders.CONTENT_TYPE]!!.first()).isEqualTo(MediaType.APPLICATION_JSON)
            }
            verify(exactly = 1) {
                client.post().uri(testUrl)
                requestSpec.bodyValue(body)
                requestSpec.retrieve().bodyToMono(EuToxRiskValidatorResponse::class.java).block()
            }
        }

    @Test
    fun validateWhenNotApplicable() =
        runTest {
            val submission =
                basicExtSubmission.copy(
                    section =
                        ExtSection(
                            type = "Study",
                            attributes = listOf(ExtAttribute(name = SKIP_VALIDATION_ATTR, value = "UPF12_MITOTOX_1")),
                        ),
                )

            testInstance.validate(submission)

            verify { client wasNot called }
        }

    @Test
    fun `validate with errors`() =
        runTest {
            val bodySlot = slot<FileSystemResource>()
            val headersSlot = slot<Consumer<HttpHeaders>>()
            val submission =
                basicExtSubmission.copy(
                    section =
                        ExtSection(
                            type = "Study",
                            files =
                                listOf(
                                    left(
                                        createNfsFile(
                                            "folder/test.xlsx",
                                            "Files/folder/test.xlsx",
                                            excelFile,
                                        ),
                                    ),
                                ),
                        ),
                )

            every { client.post().uri(testUrl) } returns requestSpec
            every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
            every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
            every {
                requestSpec.retrieve().bodyToMono(EuToxRiskValidatorResponse::class.java).block()
            } returns EuToxRiskValidatorResponse(listOf(EuToxRiskValidatorMessage("an error")))

            val error = assertThrows<CollectionValidationException> { testInstance.validate(submission) }
            assertThat(error.message).isEqualTo(
                "The submission doesn't comply with the collection requirements. Errors: [an error]",
            )

            val body = bodySlot.captured
            val headers = headersSlot.captured
            headers.andThen {
                assertThat(it[HttpHeaders.CONTENT_TYPE]!!.first()).isEqualTo(MediaType.APPLICATION_JSON)
            }
            verify(exactly = 1) {
                client.post().uri(testUrl)
                requestSpec.bodyValue(body)
                requestSpec.retrieve().bodyToMono(EuToxRiskValidatorResponse::class.java).block()
            }
        }

    @Test
    fun `submission without excel file`() =
        runTest {
            val submission =
                basicExtSubmission.copy(
                    section =
                        ExtSection(
                            type = "Study",
                            files =
                                listOf(
                                    left(createNfsFile("folder/test.txt", "Files/folder/test.txt", textFile)),
                                ),
                        ),
                )

            val error = assertThrows<CollectionValidationException> { testInstance.validate(submission) }
            assertThat(error.message).isEqualTo(
                "The submission doesn't comply with the collection requirements. Errors: [$EXCEL_FILE_REQUIRED]",
            )
        }
}
