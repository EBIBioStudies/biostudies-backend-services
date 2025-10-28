package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.DoiProperties
import ac.uk.ebi.biostd.common.properties.RetryProperties
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.InvalidDoiException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgNameException
import ac.uk.ebi.biostd.submission.exceptions.MissingAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.MissingDoiFieldException
import ac.uk.ebi.biostd.submission.exceptions.MissingTitleException
import ac.uk.ebi.biostd.submission.exceptions.RemovedDoiException
import ac.uk.ebi.biostd.submission.model.DoiRequest.Companion.BS_DOI_ID
import ac.uk.ebi.biostd.submission.service.DoiService.Companion.FILE_PARAM
import ac.uk.ebi.biostd.submission.service.DoiService.Companion.OPERATION_PARAM
import ac.uk.ebi.biostd.submission.service.DoiService.Companion.OPERATION_PARAM_VALUE
import ac.uk.ebi.biostd.submission.service.DoiService.Companion.PASSWORD_PARAM
import ac.uk.ebi.biostd.submission.service.DoiService.Companion.USER_PARAM
import ebi.ac.uk.coroutines.RetryConfig
import ebi.ac.uk.coroutines.SuspendRetryTemplate
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.extensions.title
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
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
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.util.function.Consumer

@ExtendWith(MockKExtension::class)
class DoiServiceTest(
    @param:MockK private val webClient: WebClient,
    @param:MockK private val requestSpec: RequestBodySpec,
    @param:MockK private val previousVersion: ExtSubmission,
) {
    private val retryTemplate = SuspendRetryTemplate(RetryConfig(1, 1, 1.0, 1))
    private val testInstance = DoiService(webClient, properties, retryTemplate)
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 10, 11, 0, 0, UTC).toInstant()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns mockNow
    }

    @Test
    fun `doi registration`() =
        runTest {
            val headersSlot = slot<Consumer<HttpHeaders>>()
            val bodySlot = slot<LinkedMultiValueMap<String, Any>>()
            val submission =
                submission {
                    title = "Test Submission"
                    attribute("DOI", "")

                    section("Study") {
                        section("Organization") {
                            accNo = "o1"
                            attribute("Name", "EMBL")
                        }

                        section("Author") {
                            attribute("Name", "John Doe")
                            attribute("ORCID", "12-32-45-82")
                            attribute("Affiliation", "o1", ref = true)
                        }
                    }
                }

            every { webClient.post().uri(properties.endpoint) } returns requestSpec
            every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
            every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
            every { requestSpec.retrieve().bodyToMono<String>() } returns Mono.just("OK")

            val doi = testInstance.calculateDoi(TEST_ACC_NO, submission, null)
            testInstance.registerDoi(TEST_ACC_NO, TEST_USER, submission)
            val body = bodySlot.captured
            val headers = headersSlot.captured
            val requestFile = body[FILE_PARAM]!!.first() as FileSystemResource

            assertThat(doi).isEqualTo("$BS_DOI_ID/$TEST_ACC_NO")
            assertThat(requestFile.file.readText()).isEqualToIgnoringWhitespace(EXPECTED_DOI_REQUEST)
            assertThat(body[USER_PARAM]!!.first()).isEqualTo(properties.user)
            assertThat(body[PASSWORD_PARAM]!!.first()).isEqualTo(properties.password)
            assertThat(body[OPERATION_PARAM]!!.first()).isEqualTo(OPERATION_PARAM_VALUE)
            headers.andThen { assertThat(it[CONTENT_TYPE]!!.first()).isEqualTo(MULTIPART_FORM_DATA) }
            verify(exactly = 1) {
                webClient.post().uri(properties.endpoint)
                requestSpec.bodyValue(body)
                requestSpec.retrieve().bodyToMono<String>()
            }
        }

    @Test
    fun `doi registration with single author name`() =
        runTest {
            val headersSlot = slot<Consumer<HttpHeaders>>()
            val bodySlot = slot<LinkedMultiValueMap<String, Any>>()
            val submission =
                submission {
                    title = "Test Submission"
                    attribute("DOI", "")

                    section("Study") {
                        section("Organization") {
                            accNo = "o1"
                            attribute("Name", "EMBL")
                        }

                        section("Author") {
                            attribute("Name", "Doe")
                            attribute("ORCID", "12-32-45-82")
                            attribute("Affiliation", "o1", ref = true)
                        }
                    }
                }

            every { webClient.post().uri(properties.endpoint) } returns requestSpec
            every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
            every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
            every { requestSpec.retrieve().bodyToMono<String>() } returns Mono.just("OK")

            val doi = testInstance.calculateDoi(TEST_ACC_NO, submission, null)
            testInstance.registerDoi(TEST_ACC_NO, TEST_USER, submission)
            val body = bodySlot.captured
            val headers = headersSlot.captured
            val requestFile = body[FILE_PARAM]!!.first() as FileSystemResource

            assertThat(doi).isEqualTo("$BS_DOI_ID/$TEST_ACC_NO")
            assertThat(requestFile.file.readText()).isEqualToIgnoringWhitespace(EXPECTED_DOI_REQUEST_WITH_SINGLE_NAME)
            assertThat(body[USER_PARAM]!!.first()).isEqualTo(properties.user)
            assertThat(body[PASSWORD_PARAM]!!.first()).isEqualTo(properties.password)
            assertThat(body[OPERATION_PARAM]!!.first()).isEqualTo(OPERATION_PARAM_VALUE)
            headers.andThen { assertThat(it[CONTENT_TYPE]!!.first()).isEqualTo(MULTIPART_FORM_DATA) }
            verify(exactly = 1) {
                webClient.post().uri(properties.endpoint)
                requestSpec.bodyValue(body)
                requestSpec.retrieve().bodyToMono<String>()
            }
        }

    @Test
    fun `doi registration with single author name and blank space`() =
        runTest {
            val headersSlot = slot<Consumer<HttpHeaders>>()
            val bodySlot = slot<LinkedMultiValueMap<String, Any>>()
            val submission =
                submission {
                    title = "Test Submission"
                    attribute("DOI", "")

                    section("Study") {
                        section("Organization") {
                            accNo = "o1"
                            attribute("Name", "EMBL")
                        }

                        section("Author") {
                            attribute("Name", "Doe ")
                            attribute("ORCID", "12-32-45-82")
                            attribute("Affiliation", "o1", ref = true)
                        }
                    }
                }

            every { webClient.post().uri(properties.endpoint) } returns requestSpec
            every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
            every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
            every { requestSpec.retrieve().bodyToMono<String>() } returns Mono.just("OK")

            val doi = testInstance.calculateDoi(TEST_ACC_NO, submission, null)
            testInstance.registerDoi(TEST_ACC_NO, TEST_USER, submission)
            val body = bodySlot.captured
            val headers = headersSlot.captured
            val requestFile = body[FILE_PARAM]!!.first() as FileSystemResource

            assertThat(doi).isEqualTo("$BS_DOI_ID/$TEST_ACC_NO")
            assertThat(requestFile.file.readText()).isEqualToIgnoringWhitespace(EXPECTED_DOI_REQUEST_WITH_SINGLE_NAME)
            assertThat(body[USER_PARAM]!!.first()).isEqualTo(properties.user)
            assertThat(body[PASSWORD_PARAM]!!.first()).isEqualTo(properties.password)
            assertThat(body[OPERATION_PARAM]!!.first()).isEqualTo(OPERATION_PARAM_VALUE)
            headers.andThen { assertThat(it[CONTENT_TYPE]!!.first()).isEqualTo(MULTIPART_FORM_DATA) }
            verify(exactly = 1) {
                webClient.post().uri(properties.endpoint)
                requestSpec.bodyValue(body)
                requestSpec.retrieve().bodyToMono<String>()
            }
        }

    @Test
    fun `doi registration missing author name`() =
        runTest {
            val headersSlot = slot<Consumer<HttpHeaders>>()
            val bodySlot = slot<LinkedMultiValueMap<String, Any>>()
            val submission =
                submission {
                    title = "Test Submission"
                    attribute("DOI", "")

                    section("Study") {
                        section("organization") {
                            accNo = "o1"
                            attribute("Name", "EMBL")
                        }

                        section("author") {
                            attribute("P.I.", "John Doe")
                            attribute("ORCID", "12-32-45-82")
                            attribute("Affiliation", "o1", ref = true)
                        }
                    }
                }

            every { webClient.post().uri(properties.endpoint) } returns requestSpec
            every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
            every { requestSpec.headers(capture(headersSlot)) } returns requestSpec
            every { requestSpec.retrieve().bodyToMono<String>() } returns Mono.just("OK")

            val doi = testInstance.calculateDoi(TEST_ACC_NO, submission, null)
            testInstance.registerDoi(TEST_ACC_NO, TEST_USER, submission)
            val body = bodySlot.captured
            val headers = headersSlot.captured
            val requestFile = body[FILE_PARAM]!!.first() as FileSystemResource

            assertThat(doi).isEqualTo("$BS_DOI_ID/$TEST_ACC_NO")
            assertThat(requestFile.file.readText()).isEqualToIgnoringWhitespace(EXPECTED_DOI_REQUEST_WITHOUT_CONTRIBUTORS)
            assertThat(body[USER_PARAM]!!.first()).isEqualTo(properties.user)
            assertThat(body[PASSWORD_PARAM]!!.first()).isEqualTo(properties.password)
            assertThat(body[OPERATION_PARAM]!!.first()).isEqualTo(OPERATION_PARAM_VALUE)
            headers.andThen { assertThat(it[CONTENT_TYPE]!!.first()).isEqualTo(MULTIPART_FORM_DATA) }
            verify(exactly = 1) {
                webClient.post().uri(properties.endpoint)
                requestSpec.bodyValue(body)
                requestSpec.retrieve().bodyToMono<String>()
            }
        }

    @Test
    fun `doi not requested`() {
        val submission =
            submission {
                title = "Test Submission"

                section("Study") {
                    attribute("Type", "Experiment")
                }
            }

        assertThat(testInstance.calculateDoi(TEST_ACC_NO, submission, null)).isNull()
    }

    @Test
    fun `already existing DOI`() {
        val previousVersionDoi = "$BS_DOI_ID/$TEST_ACC_NO"
        val submission =
            submission {
                title = "Test Submission"
                attribute("DOI", "$BS_DOI_ID/$TEST_ACC_NO")

                section("Study") {
                    attribute("Type", "Experiment")
                }
            }

        every { previousVersion.doi } returns previousVersionDoi

        val doi = testInstance.calculateDoi(TEST_ACC_NO, submission, previousVersion)
        assertThat(doi).isEqualTo(previousVersionDoi)
    }

    @Test
    fun `invalid given DOI`() {
        val doi = "10.287.71/$TEST_ACC_NO"
        val previousDoi = "$BS_DOI_ID/$TEST_ACC_NO"
        val submission =
            submission {
                title = "Test Submission"
                attribute("DOI", doi)

                section("Study") {
                    attribute("Type", "Experiment")
                }
            }

        every { previousVersion.doi } returns previousDoi

        val exception =
            assertThrows<InvalidDoiException> {
                testInstance.calculateDoi(TEST_ACC_NO, submission, previousVersion)
            }
        assertThat(exception.message).isEqualTo("The given DOI '$doi' should match the previous DOI '$previousDoi'")
    }

    @Test
    fun `removed DOI`() {
        val previousDoi = "$BS_DOI_ID/$TEST_ACC_NO"
        val submission =
            submission {
                title = "Test Submission"
                attribute("DOI", null)

                section("Study") {
                    attribute("Type", "Experiment")
                }
            }

        every { previousVersion.doi } returns previousDoi

        val exception =
            assertThrows<RemovedDoiException> {
                testInstance.calculateDoi(TEST_ACC_NO, submission, previousVersion)
            }
        assertThat(exception.message).isEqualTo("The previous DOI: '$previousDoi' cannot be removed")
    }

    @Test
    fun `missing title`() {
        val submission =
            submission {
                attribute("DOI", "")
            }

        val exception = assertThrows<MissingTitleException> { testInstance.calculateDoi(TEST_ACC_NO, submission, null) }
        assertThat(exception.message).isEqualTo("A title is required for DOI registration")
    }

    @Test
    fun `missing organization`() {
        val sub =
            submission {
                title = "Test Submission"
                attribute("DOI", "")

                section("Study") {
                    section("Author") {
                        attribute("Name", "John Doe")
                    }
                }
            }

        val exception = assertThrows<MissingDoiFieldException> { testInstance.calculateDoi(TEST_ACC_NO, sub, null) }
        assertThat(exception.message).isEqualTo("The required DOI field 'Organization' could not be found")
    }

    @Test
    fun `missing organization accNo`() {
        val sub =
            submission {
                title = "Test Submission"
                attribute("DOI", "")

                section("Study") {
                    section("Organization") {
                        attribute("Name", "EMBL")
                    }
                }
            }

        val exception = assertThrows<InvalidOrgException> { testInstance.calculateDoi(TEST_ACC_NO, sub, null) }
        assertThat(exception.message).isEqualTo("Organizations are required to have an accession")
    }

    @Test
    fun `missing organization name`() {
        val sub =
            submission {
                title = "Test Submission"
                attribute("DOI", "")

                section("Study") {
                    section("Organization") {
                        accNo = "o1"
                        attribute("Name", "American Society")
                    }

                    section("Organisation") {
                        accNo = "o2"
                        attribute("Name", "EMBL")
                    }

                    section("organization") {
                        accNo = "o3"
                        attribute("Research Associate", "Astrazeneca")
                    }
                }
            }

        val exception = assertThrows<InvalidOrgNameException> { testInstance.calculateDoi(TEST_ACC_NO, sub, null) }
        assertThat(exception.message).isEqualTo("The following organization name is empty: 'o3'")
    }

    @Test
    fun `missing affiliation`() {
        val sub =
            submission {
                title = "Test Submission"
                attribute("DOI", "")

                section("Study") {
                    section("Organization") {
                        accNo = "o1"
                        attribute("Name", "EMBL")
                    }

                    section("Author") {
                        attribute("Name", "John Doe")
                        attribute("ORCID", "12-32-45-82")
                    }
                }
            }

        val exception =
            assertThrows<MissingAuthorAffiliationException> {
                testInstance.calculateDoi(TEST_ACC_NO, sub, null)
            }
        assertThat(exception.message).isEqualTo("Authors are required to have an affiliation")
    }

    @Test
    fun `invalid affiliation`() {
        val sub =
            submission {
                title = "Test Submission"
                attribute("DOI", null)

                section("Study") {
                    section("Organization") {
                        accNo = "o1"
                        attribute("Name", "EMBL")
                    }

                    section("Author") {
                        attribute("Name", "John Doe")
                        attribute("ORCID", "12-32-45-82")
                        attribute("affiliation", "o2", ref = true)
                    }
                }
            }

        val exception =
            assertThrows<InvalidAuthorAffiliationException> {
                testInstance.calculateDoi(TEST_ACC_NO, sub, null)
            }
        assertThat(exception.message)
            .isEqualTo("The organization 'o2' affiliated to the author 'John Doe' could not be found")
    }

    companion object {
        private const val TEST_ACC_NO = "S-TEST123"
        private const val TEST_USER = "user@ebi.ac.uk"

        private val properties =
            DoiProperties(
                endpoint = "https://test-endpoint.org",
                uiUrl = "https://www.biostudies.ac.uk",
                email = "biostudies@ebi.ac.uk",
                user = "a-user",
                password = "a-password",
                retry = RetryProperties(1, 1, 1.0, 1),
            )

        private const val EXPECTED_DOI_REQUEST = """
            <doi_batch
                xmlns="http://www.crossref.org/schema/4.4.1"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="4.4.1" xsi:schemaLocation="http://www.crossref.org/schema/4.4.1
                http://www.crossref.org/schema/deposit/crossref4.4.1.xsd">
                <head>
                    <doi_batch_id>1600683060</doi_batch_id>
                    <timestamp>1600683060</timestamp>
                    <depositor>
                        <depositor_name>EMBL-EBI</depositor_name>
                        <email_address>biostudies@ebi.ac.uk</email_address>
                    </depositor>
                    <registrant>EMBL-EBI</registrant>
                </head>
                <body>
                    <database>
                        <database_metadata language="en">
                            <titles>
                                <title>BioStudies Database</title>
                            </titles>
                        </database_metadata>
                        <dataset>
                            <contributors>
                                <person_name contributor_role="author" sequence="first">
                                    <given_name>John</given_name>
                                    <surname>Doe</surname>
                                    <affiliation>EMBL</affiliation>
                                    <ORCID authenticated="false">https://orcid.org/12-32-45-82</ORCID>
                                </person_name>
                            </contributors>
                            <titles>
                                <title>Test Submission</title>
                            </titles>
                            <doi_data>
                                <doi>10.6019/S-TEST123</doi>
                                <resource>https://www.biostudies.ac.uk/studies/S-TEST123</resource>
                            </doi_data>
                        </dataset>
                    </database>
                </body>
            </doi_batch>
        """

        private const val EXPECTED_DOI_REQUEST_WITH_SINGLE_NAME = """
            <doi_batch
                xmlns="http://www.crossref.org/schema/4.4.1"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="4.4.1" xsi:schemaLocation="http://www.crossref.org/schema/4.4.1
                http://www.crossref.org/schema/deposit/crossref4.4.1.xsd">
                <head>
                    <doi_batch_id>1600683060</doi_batch_id>
                    <timestamp>1600683060</timestamp>
                    <depositor>
                        <depositor_name>EMBL-EBI</depositor_name>
                        <email_address>biostudies@ebi.ac.uk</email_address>
                    </depositor>
                    <registrant>EMBL-EBI</registrant>
                </head>
                <body>
                    <database>
                        <database_metadata language="en">
                            <titles>
                                <title>BioStudies Database</title>
                            </titles>
                        </database_metadata>
                        <dataset>
                            <contributors>
                                <person_name contributor_role="author" sequence="first">
                                    <surname>Doe</surname>
                                    <affiliation>EMBL</affiliation>
                                    <ORCID authenticated="false">https://orcid.org/12-32-45-82</ORCID>
                                </person_name>
                            </contributors>
                            <titles>
                                <title>Test Submission</title>
                            </titles>
                            <doi_data>
                                <doi>10.6019/S-TEST123</doi>
                                <resource>https://www.biostudies.ac.uk/studies/S-TEST123</resource>
                            </doi_data>
                        </dataset>
                    </database>
                </body>
            </doi_batch>
        """

        private const val EXPECTED_DOI_REQUEST_WITHOUT_CONTRIBUTORS = """
            <doi_batch
                xmlns="http://www.crossref.org/schema/4.4.1"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="4.4.1" xsi:schemaLocation="http://www.crossref.org/schema/4.4.1
                http://www.crossref.org/schema/deposit/crossref4.4.1.xsd">
                <head>
                    <doi_batch_id>1600683060</doi_batch_id>
                    <timestamp>1600683060</timestamp>
                    <depositor>
                        <depositor_name>EMBL-EBI</depositor_name>
                        <email_address>biostudies@ebi.ac.uk</email_address>
                    </depositor>
                    <registrant>EMBL-EBI</registrant>
                </head>
                <body>
                    <database>
                        <database_metadata language="en">
                            <titles>
                                <title>BioStudies Database</title>
                            </titles>
                        </database_metadata>
                        <dataset>
                            <titles>
                                <title>Test Submission</title>
                            </titles>
                            <doi_data>
                                <doi>10.6019/S-TEST123</doi>
                                <resource>https://www.biostudies.ac.uk/studies/S-TEST123</resource>
                            </doi_data>
                        </dataset>
                    </database>
                </body>
            </doi_batch>
        """
    }
}
