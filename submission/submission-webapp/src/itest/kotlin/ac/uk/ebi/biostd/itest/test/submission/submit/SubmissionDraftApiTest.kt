package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.JSON
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.RequestStatus.SUBMITTED
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.flow.toList
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
import java.time.Instant
import java.time.OffsetDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Transactional
class SubmissionDraftApiTest(
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val requestRepository: SubmissionRequestPersistenceService,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var superWebClient: BioWebClient
    private lateinit var regularWebClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureSequence("S-BSST")

            securityTestService.ensureUserRegistration(SuperUser)
            superWebClient = getWebClient(serverPort, SuperUser)

            securityTestService.ensureUserRegistration(RegularUser)
            regularWebClient = getWebClient(serverPort, RegularUser)
        }

    @Test
    fun `12-1 get draft submission when draft does not exist but submission does`() =
        runTest {
            val pageTab =
                jsonObj {
                    "accno" to "ABC-123"
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }.toString()

            superWebClient.submit(pageTab, JSON)

            val draftSubmission = superWebClient.getSubmissionDraft("ABC-123")
            assertThat(draftSubmission.key).isEqualTo(draftSubmission.key)
            superWebClient.deleteSubmissionDraft(draftSubmission.key)
        }

    @Test
    fun `12-2 create and get submission draft`() =
        runTest {
            val pageTab =
                jsonObj {
                    "accno" to "ABC-124"
                    "type" to "Study"
                }.toString()

            val draftSubmission = superWebClient.createSubmissionDraft(pageTab)

            val resultDraft = superWebClient.getSubmissionDraft(draftSubmission.key)
            assertEquals(resultDraft.content.toString(), pageTab, false)
            superWebClient.deleteSubmissionDraft(draftSubmission.key)
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

            val firstVersion = superWebClient.createSubmissionDraft(pageTab)
            superWebClient.updateSubmissionDraft(firstVersion.key, "{ \"value\": 1 }")

            val secondVersion = superWebClient.getSubmissionDraft(firstVersion.key)
            assertThat(firstVersion)
            assertEquals(secondVersion.content.toString(), updatedValue, false)
            assertThat(firstVersion.modificationTime).isBefore(secondVersion.modificationTime)
            superWebClient.deleteSubmissionDraft(firstVersion.key)
        }

    @Test
    fun `12-4 delete submission draft after submission`() =
        runTest {
            val pageTab =
                jsonObj {
                    "accno" to "ABC-126"
                    "title" to "From Draft"
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }.toString()

            val draft = superWebClient.createSubmissionDraft(pageTab)

            superWebClient.submitFromDraft(draft.key)

            assertThat(superWebClient.getAllSubmissionDrafts()).isEmpty()
        }

    @Test
    fun `12-5 get draft submission when neither draft nor submission exists`() =
        runTest {
            assertThrows<WebClientException> { superWebClient.getSubmissionDraft("ABC-127") }
        }

    @Test
    fun `12-6 delete a draft directly`() =
        runTest {
            val pageTab =
                jsonObj {
                    "accno" to "ABC-128"
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }.toString()
            superWebClient.submit(pageTab, JSON)

            superWebClient.deleteSubmissionDraft("ABC-128")

            assertThat(requestRepository.findEditableRequest("ABC-128", SuperUser.email))
        }

    @Test
    fun `12-7 re submit from draft`() =
        runTest {
            superWebClient.submit(
                jsonObj {
                    "accno" to "ABC-129"
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "Source"
                            "value" to "PageTab"
                            "reference" to false
                            "nameAttrs" to jsonArray()
                            "valueAttrs" to jsonArray()
                        }, {
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }.toString(),
                JSON,
            )

            val version1 = superWebClient.getExtByAccNo("ABC-129")
            assertThat(version1.attributes.first().name).isEqualTo("Source")
            assertThat(version1.attributes.first().value).isEqualTo("PageTab")

            val updatedDraft =
                jsonObj {
                    "accno" to "ABC-129"
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "Source"
                            "value" to "Draft"
                            "reference" to false
                            "nameAttrs" to jsonArray()
                            "valueAttrs" to jsonArray()
                        }, {
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }.toString()
            superWebClient.getSubmissionDraft("ABC-129")

            superWebClient.updateSubmissionDraft("ABC-129", updatedDraft)

            superWebClient.submitFromDraft("ABC-129")

            val version2 = superWebClient.getExtByAccNo("ABC-129")
            assertThat(version2.attributes.first().name).isEqualTo("Source")
            assertThat(version2.attributes.first().value).isEqualTo("Draft")

            assertThat(requestRepository.findRequestDrafts(SuperUser.email).toList()).isEmpty()

            val request = requestRepository.getRequest("ABC-129", 2)
            assertThat(request.accNo).isEqualTo("ABC-129")
            assertThat(request.draft).isEqualTo(updatedDraft)
        }

    @Test
    fun `12-8 update a submission already submitted draft`() =
        runTest {
            val accNo = "ABC-130"
            val newSubmission =
                superWebClient
                    .submit(
                        jsonObj {
                            "accno" to accNo
                            "attributes" to
                                jsonArray({
                                    "name" to "ReleaseDate"
                                    "value" to OffsetDateTime.now().toStringDate()
                                })
                            "section" to
                                jsonObj {
                                    "type" to "Study"
                                }
                            "type" to "submission"
                        }.toString(),
                    ).body
            assertThat(newSubmission.section.type).isEqualTo("Study")

            val firstVersion = superWebClient.getSubmissionDraft(accNo)
            superWebClient.updateSubmissionDraft(
                accNo,
                jsonObj {
                    "accno" to accNo
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                    "section" to
                        jsonObj {
                            "type" to "Another"
                        }
                    "type" to "submission"
                }.toString(),
            )

            val secondVersion = superWebClient.getSubmissionDraft(accNo)
            val updatedSubmission = superWebClient.submitFromDraft(accNo).body
            assertThat(updatedSubmission.section.type).isEqualTo("Another")
            assertThat(secondVersion.modificationTime).isAfter(firstVersion.modificationTime)

            superWebClient.getSubmissionDraft(accNo)
            superWebClient.updateSubmissionDraft(
                accNo,
                jsonObj {
                    "accno" to accNo
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                    "section" to
                        jsonObj {
                            "type" to "Yet-Another"
                        }
                    "type" to "submission"
                }.toString(),
            )

            val thirdVersion = superWebClient.getSubmissionDraft(accNo)
            assertThat(thirdVersion.content.getProperty("section.type")).isEqualTo("Yet-Another")
            assertThat(thirdVersion.modificationTime).isAfter(secondVersion.modificationTime)
            superWebClient.deleteSubmissionDraft(accNo)
        }

    @Test
    fun `12-9 submit json when a draft already exists`() =
        runTest {
            val accNo = "ABC-131"
            val subV1 =
                jsonObj {
                    "accno" to accNo
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                    "section" to
                        jsonObj {
                            "type" to "Study"
                        }
                    "type" to "submission"
                }.toString()

            assertThat(superWebClient.submit(subV1)).isSuccessful()
            superWebClient.getSubmissionDraft(accNo)
            superWebClient.updateSubmissionDraft(
                accNo,
                jsonObj {
                    "accno" to accNo
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                    "section" to
                        jsonObj {
                            "type" to "ByDraft"
                        }
                    "type" to "submission"
                }.toString(),
            )

            val subV2 =
                jsonObj {
                    "accno" to accNo
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                    "section" to
                        jsonObj {
                            "type" to "Another"
                        }
                    "type" to "submission"
                }.toString()
            val response = superWebClient.submit(subV2)
            assertThat(response).isSuccessful()

            val updatedSubmission = response.body
            assertThat(updatedSubmission.section.type).isEqualTo("Another")
        }

    @Test
    fun `12-10 update a draft with an processing request`() =
        runTest {
            val accNo = "ABC-132"
            val sub =
                jsonObj {
                    "accno" to accNo
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                    "section" to
                        jsonObj {
                            "type" to "Study"
                        }
                    "type" to "submission"
                }.toString()

            assertThat(superWebClient.submit(sub)).isSuccessful()
            superWebClient.getSubmissionDraft(accNo)
            requestRepository.setDraftStatus(accNo, SuperUser.email, SUBMITTED, Instant.now())

            val exception =
                assertThrows<WebClientException> {
                    superWebClient.updateSubmissionDraft(
                        accNo,
                        jsonObj {
                            "accno" to accNo
                            "attributes" to
                                jsonArray({
                                    "name" to "ReleaseDate"
                                    "value" to OffsetDateTime.now().toStringDate()
                                })
                            "section" to
                                jsonObj {
                                    "type" to "ByDraft"
                                }
                            "type" to "submission"
                        }.toString(),
                    )
                }
            val error = "Request 'ABC-132' is being processed. Submission request draft operations are blocked."
            assertThat(exception).hasMessageContaining(error)
        }

    @Test
    fun `12-11 create a draft with an processing request`() =
        runTest {
            val accNo = "ABC-133"
            val sub =
                jsonObj {
                    "accno" to accNo
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                    "section" to
                        jsonObj {
                            "type" to "Study"
                        }
                    "type" to "submission"
                }.toString()

            assertThat(superWebClient.submit(sub)).isSuccessful()
            superWebClient.getSubmissionDraft(accNo)
            requestRepository.setDraftStatus(accNo, SuperUser.email, SUBMITTED, Instant.now())

            val exception = assertThrows<WebClientException> { superWebClient.getSubmissionDraft("ABC-133") }
            val error = "Request 'ABC-133' is being processed. Submission request draft operations are blocked."
            assertThat(exception).hasMessageContaining(error)
        }

    @Test
    fun `12-12 re submit draft of another user submission with admin user`() =
        runTest {
            val pageTab =
                jsonObj {
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }.toString()
            val accNo = regularWebClient.submit(pageTab, JSON).body.accNo

            val newPageTab =
                jsonObj {
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "Source"
                            "value" to "PageTab"
                        }, {
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }.toString()
            val draft = superWebClient.getSubmissionDraft(accNo).key
            superWebClient.updateSubmissionDraft(accNo, newPageTab)

            superWebClient.submitFromDraft(draft)
            val submission = superWebClient.getExtByAccNo(accNo)
            assertThat(submission.attributes.first().name).isEqualTo("Source")
            assertThat(submission.attributes.first().value).isEqualTo("PageTab")
        }

    @Test
    fun `12-13 multiple user submission of the same submission draft`() =
        runTest {
            val pageTab =
                jsonObj {
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }.toString()
            val userSubmission =
                jsonObj {
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "User"
                            "value" to "Regular"
                        }, {
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }
            val superUserSubmission =
                jsonObj {
                    "type" to "Study"
                    "attributes" to
                        jsonArray({
                            "name" to "User"
                            "value" to "SuperUser"
                        }, {
                            "name" to "ReleaseDate"
                            "value" to OffsetDateTime.now().toStringDate()
                        })
                }

            val accNo = regularWebClient.submit(pageTab, JSON).body.accNo
            val superUserDraft = superWebClient.getSubmissionDraft(accNo).key
            val regularUserDraft = regularWebClient.getSubmissionDraft(accNo).key

            // Submit user submission
            regularWebClient.updateSubmissionDraft(accNo, userSubmission.toString())
            regularWebClient.submitFromDraft(regularUserDraft)
            val submission = superWebClient.getExtByAccNo(accNo)
            assertThat(submission.version).isEqualTo(3)
            assertThat(submission.attributes.first().name).isEqualTo("User")
            assertThat(submission.attributes.first().value).isEqualTo("Regular")

            // Submit super user submission
            superWebClient.updateSubmissionDraft(accNo, superUserSubmission.toString())
            superWebClient.submitFromDraft(superUserDraft)
            val superUserSubmissionResult = superWebClient.getExtByAccNo(accNo)
            assertThat(superUserSubmissionResult.version).isEqualTo(2)
            assertThat(superUserSubmissionResult.attributes.first().name).isEqualTo("User")
            assertThat(superUserSubmissionResult.attributes.first().value).isEqualTo("SuperUser")
        }

    private object RegularUser : TestUser {
        override val username = "Regular User"
        override val email = "regular-drafts@ebi.ac.uk"
        override val password = "678910"
        override val superUser = false
        override val storageMode: StorageMode = StorageMode.NFS

        override fun asRegisterRequest() = RegisterRequest(username, email, password, notificationsEnabled = true)
    }
}
