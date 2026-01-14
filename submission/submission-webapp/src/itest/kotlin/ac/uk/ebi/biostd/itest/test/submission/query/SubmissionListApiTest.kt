package ac.uk.ebi.biostd.itest.test.submission.query

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.common.properties.StorageMode
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.TestUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.storageMode
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.persistence.doc.migrations.ensureSubmissionIndexes
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.ext.createFile
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URLEncoder.encode

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SubmissionListApiTest(
    @param:Autowired val securityTestService: SecurityTestService,
    @param:Autowired val mongoTemplate: ReactiveMongoTemplate,
    @param:LocalServerPort val serverPort: Int,
) {
    private lateinit var superUserClient: BioWebClient
    private lateinit var regularUserClient: BioWebClient

    @BeforeAll
    fun init() =
        runBlocking {
            securityTestService.ensureSequence("S-BSST")
            mongoTemplate.ensureSubmissionIndexes()

            securityTestService.ensureUserRegistration(SuperUser)
            superUserClient = getWebClient(serverPort, SuperUser)

            securityTestService.ensureUserRegistration(RegularUser)
            regularUserClient = getWebClient(serverPort, RegularUser)

            for (idx in 11..20) {
                assertThat(superUserClient.submit(getSimpleSubmission(idx), TSV)).isSuccessful()
            }

            val params = SubmitParameters(storageMode = storageMode)
            for (idx in 21..31) {
                val submission = tempFolder.createFile("submission$idx.tsv", getSimpleSubmission(idx))
                assertThat(superUserClient.submitMultipart(submission, params)).isSuccessful()
            }

            superUserClient.deleteSubmission("LIST-API-31")
        }

    @Test
    fun `13-1 get submission list`() =
        runTest {
            val submissionList = superUserClient.getSubmissions()

            assertThat(submissionList).isNotNull
            assertThat(submissionList).hasSize(15)
        }

    @Test
    fun `13-2 get submission list by accession`() =
        runTest {
            val submissionList =
                superUserClient.getSubmissions(
                    mapOf(
                        "accNo" to "LIST-API-17",
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.accno).isEqualTo("LIST-API-17")
                assertThat(it.title).isEqualTo("Simple Submission 17 - list-api-keyword-17")
                assertThat(it.status).isEqualTo("PROCESSED")
            }
        }

    @Test
    fun `13-3 get direct submission list by accession`() =
        runTest {
            val submissionList =
                superUserClient.getSubmissions(
                    mapOf(
                        "accNo" to "LIST-API-27",
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.accno).isEqualTo("LIST-API-27")
                assertThat(it.title).isEqualTo("Simple Submission 27 - list-api-keyword-27")
                assertThat(it.status).isEqualTo("PROCESSED")
            }
        }

    @Test
    fun `13-4 get submission list by keywords`() =
        runTest {
            val submissionList =
                superUserClient.getSubmissions(
                    mapOf(
                        "keywords" to "list-api-keyword-20",
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.title).contains("list-api-keyword-20")
            }
        }

    @Test
    fun `13-6 get submission list pagination`() =
        runTest {
            val submissionList =
                superUserClient.getSubmissions(
                    mapOf(
                        "offset" to 15,
                    ),
                )

            assertThat(submissionList).hasSize(5)
        }

    @Test
    fun `13-7 get submissions with submission or section title - superUser`() {
        suspend fun assertFound(keywords: String) {
            val submissionList =
                superUserClient.getSubmissions(
                    mapOf(
                        "keywords" to keywords,
                    ),
                )

            assertThat(submissionList)
                .withFailMessage { "Could not find submission using keywords='$keywords'" }
                .satisfiesOnlyOnce {
                    assertThat(it.accno).isEqualTo("SPACE-123")
                    assertThat(it.title).isEqualTo("Submission the title")
                    assertThat(it.status).isEqualTo("PROCESSED")
                }
        }

        runTest {
            val submission =
                tsv {
                    line("Submission", "SPACE-123")
                    line("Title", "Submission hello world")
                    line()

                    line("Study")
                    line("Title", "Submission the title")
                    line()
                }.toString()

            assertThat(superUserClient.submit(submission, TSV)).isSuccessful()

            assertFound(keywords = "hello world")
            assertFound(keywords = "world")
            assertFound(keywords = "hello")
            assertFound(keywords = "the title")
            assertFound(keywords = "title")
        }
    }

    @Test
    fun `13-8 get submissions with submission or section title - normalUser`() {
        runTest {
            suspend fun assertFound(keywords: String) {
                val submissionList =
                    regularUserClient.getSubmissions(
                        mapOf(
                            "keywords" to keywords,
                        ),
                    )

                assertThat(submissionList)
                    .withFailMessage { "Could not find submission using keywords='$keywords'" }
                    .satisfiesOnlyOnce {
                        assertThat(it.title).isEqualTo("Submission beta gama")
                        assertThat(it.status).isEqualTo("PROCESSED")
                    }
            }

            val submission =
                tsv {
                    line("Submission")
                    line("Title", "Submission alpha omega")
                    line()

                    line("Study")
                    line("Title", "Submission beta gama")
                    line()
                }.toString()

            assertThat(regularUserClient.submit(submission, TSV)).isSuccessful()

            assertFound(keywords = "alpha omega")
            assertFound(keywords = "alpha")
            assertFound(keywords = "omega")
            assertFound(keywords = "beta gama")
            assertFound(keywords = "beta")
            assertFound(keywords = "gama")
        }
    }

    @Test
    fun `13-9 search submission with spaces`() =
        runTest {
            val submission =
                tsv {
                    line("Submission", "SECT-125")
                    line("Title", "the Submission spaces title")
                    line()

                    line("Study")
                    line("Title", "the Submission spaces title")
                    line()
                }.toString()

            assertThat(superUserClient.submit(submission, TSV)).isSuccessful()

            val submissionList =
                superUserClient.getSubmissions(
                    mapOf(
                        "keywords" to encode("spaces title", "UTF-8"),
                    ),
                )

            assertThat(submissionList).satisfiesOnlyOnce {
                assertThat(it.accno).isEqualTo("SECT-125")
                assertThat(it.title).isEqualTo("the Submission spaces title")
                assertThat(it.status).isEqualTo("PROCESSED")
            }
        }

    @Test
    fun `13-10 latest updated submission should appear first`() =
        runTest {
            superUserClient.submit(getSimpleSubmission(19), TSV)

            val submissionList =
                superUserClient.getSubmissions(
                    mapOf("keywords" to "list-api-keyword"),
                )

            assertThat(submissionList.first().accno).isEqualTo("LIST-API-19")
        }

    private fun getSimpleSubmission(idx: Int) =
        tsv {
            line("Submission", "LIST-API-$idx")
            line("Title", "Simple Submission $idx - list-api-keyword-$idx")
            line("ReleaseDate", "2119-01-$idx")
            line()
        }.toString()

    object RegularUser : TestUser {
        override val username = "Regular Collection User"
        override val email = "regular-for-listing@ebi.ac.uk"
        override val password = "12345"
        override val superUser = false
        override val storageMode = StorageMode.NFS
    }


    /**
     * Represents a bio studies super user.
     */
    object SuperUser : TestUser {
        override val username = "Super User"
        override val email = "biostudies-mgmt-list@ebi.ac.uk"
        override val password = "12345"
        override val superUser = true
        override val storageMode = StorageMode.NFS
    }
}
