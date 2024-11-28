package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import uk.ac.ebi.serialization.extensions.getProperty

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Transactional
class SubmissionDraftApiTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val requestRepository: SubmissionRequestPersistenceService,
    @Autowired val draftPersistenceService: SubmissionDraftPersistenceService,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var webClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureUserRegistration(SuperUser)
            webClient = getWebClient(serverPort, SuperUser)
        }

    @Test
    fun `12-1 get draft submission when draft does not exist but submission does`() =
        runTest {
            val pageTab =
                jsonObj {
                    "accno" to "ABC-123"
                    "type" to "Study"
                }.toString()

            webClient.submit(pageTab, JSON)

            val draftSubmission = webClient.getSubmissionDraft("ABC-123")
            assertThat(draftSubmission.key).isEqualTo("ABC-123")
            webClient.deleteSubmissionDraft(draftSubmission.key)
        }

    @Test
    fun `12-2 create and get submission draft`() =
        runTest {
            val pageTab =
                jsonObj {
                    "accno" to "ABC-124"
                    "type" to "Study"
                }.toString()

            val draftSubmission = webClient.createSubmissionDraft(pageTab)

            val resultDraft = webClient.getSubmissionDraft(draftSubmission.key)
            assertEquals(resultDraft.content.toString(), pageTab, false)
            webClient.deleteSubmissionDraft(draftSubmission.key)
        }

    @Test
    fun `12-3 create and update submission draft`() =
        runTest {
            val updatedValue = "{ \"value\": 1 }"
            val pageTab =
                jsonObj {
                    "accno" to "ABC-125"
                    "type" to "Study"
                }.toString()

            val firstVersion = webClient.createSubmissionDraft(pageTab)
            webClient.updateSubmissionDraft(firstVersion.key, "{ \"value\": 1 }")

            val secondVersion = webClient.getSubmissionDraft(firstVersion.key)
            assertThat(firstVersion)
            assertEquals(secondVersion.content.toString(), updatedValue, false)
            assertThat(firstVersion.modificationTime).isBefore(secondVersion.modificationTime)
            webClient.deleteSubmissionDraft(firstVersion.key)
        }

    @Test
    fun `12-4 delete submission draft after submission`() =
        runTest {
            val pageTab =
                jsonObj {
                    "accno" to "ABC-126"
                    "title" to "From Draft"
                }.toString()
            val draft = webClient.createSubmissionDraft(pageTab)

            webClient.submitFromDraft(draft.key)

            assertThat(webClient.getAllSubmissionDrafts()).isEmpty()
        }

    @Test
    fun `12-5 get draft submission when neither draft nor submission exists`() =
        runTest {
            assertThrows<WebClientException> { webClient.getSubmissionDraft("ABC-127") }
        }

    @Test
    fun `12-6 delete a draft directly`() =
        runTest {
            val pageTab =
                jsonObj {
                    "accno" to "ABC-128"
                    "type" to "Study"
                }.toString()
            webClient.submit(pageTab, JSON)

            webClient.deleteSubmissionDraft("ABC-128")

            assertThat(draftPersistenceService.findSubmissionDraft(SuperUser.email, "ABC-128")).isNull()
        }

    @Test
    fun `12-7 re submit from draft`() =
        runTest {
            webClient.submit(
                jsonObj {
                    "accno" to "ABC-129"
                    "type" to "Study"
                    "attributes" to
                        jsonArray(
                            jsonObj {
                                "name" to "Source"
                                "value" to "PageTab"
                                "reference" to false
                                "nameAttrs" to jsonArray()
                                "valueAttrs" to jsonArray()
                            },
                        )
                }.toString(),
                JSON,
            )

            val version1 = webClient.getExtByAccNo("ABC-129")
            assertThat(version1.attributes.first().name).isEqualTo("Source")
            assertThat(version1.attributes.first().value).isEqualTo("PageTab")

            val updatedDraft =
                jsonObj {
                    "accno" to "ABC-129"
                    "type" to "Study"
                    "attributes" to
                        jsonArray(
                            jsonObj {
                                "name" to "Source"
                                "value" to "Draft"
                                "reference" to false
                                "nameAttrs" to jsonArray()
                                "valueAttrs" to jsonArray()
                            },
                        )
                }.toString()
            webClient.getSubmissionDraft("ABC-129")
            webClient.updateSubmissionDraft("ABC-129", updatedDraft)

            webClient.submitFromDraft("ABC-129")

            val version2 = webClient.getExtByAccNo("ABC-129")
            assertThat(version2.attributes.first().name).isEqualTo("Source")
            assertThat(version2.attributes.first().value).isEqualTo("Draft")
            assertThat(draftPersistenceService.findSubmissionDraft(SuperUser.email, "ABC-129")).isNull()

            val request = requestRepository.getRequest("ABC-129", 2)
            assertThat(request.key).isEqualTo("ABC-129")
            assertThat(request.draft).isEqualTo(updatedDraft)
        }

    @Test
    fun `12-8 update a submission already submitted draft`() =
        runTest {
            val accNo = "ABC-130"
            val newSubmission =
                webClient
                    .submit(
                        jsonObj {
                            "accno" to accNo
                            "section" to
                                jsonObj {
                                    "type" to "Study"
                                }
                            "type" to "submission"
                        }.toString(),
                    ).body
            assertThat(newSubmission.section.type).isEqualTo("Study")

            val firstVersion = webClient.getSubmissionDraft(accNo)
            webClient.updateSubmissionDraft(
                accNo,
                jsonObj {
                    "accno" to accNo
                    "section" to
                        jsonObj {
                            "type" to "Another"
                        }
                    "type" to "submission"
                }.toString(),
            )

            val secondVersion = webClient.getSubmissionDraft(accNo)
            val updatedSubmission = webClient.submitFromDraft(accNo).body
            assertThat(updatedSubmission.section.type).isEqualTo("Another")
            assertThat(secondVersion.modificationTime).isAfter(firstVersion.modificationTime)

            webClient.getSubmissionDraft(accNo)
            webClient.updateSubmissionDraft(
                accNo,
                jsonObj {
                    "accno" to accNo
                    "section" to
                        jsonObj {
                            "type" to "Yet-Another"
                        }
                    "type" to "submission"
                }.toString(),
            )

            val thirdVersion = webClient.getSubmissionDraft(accNo)
            assertThat(thirdVersion.content.getProperty("section.type")).isEqualTo("Yet-Another")
            assertThat(thirdVersion.modificationTime).isAfter(secondVersion.modificationTime)
            webClient.deleteSubmissionDraft(accNo)
        }
}
