package ac.uk.ebi.biostd.itest.test.submission.submit

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat.TSV
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.itest.common.SecurityTestService
import ac.uk.ebi.biostd.itest.entities.DefaultUser
import ac.uk.ebi.biostd.itest.entities.SuperUser
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.ftpPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.submissionPath
import ac.uk.ebi.biostd.itest.itest.ITestListener.Companion.tempFolder
import ac.uk.ebi.biostd.itest.itest.getWebClient
import ac.uk.ebi.biostd.itest.test.collection.ListCollectionsTest.CollectionUser
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ADMIN
import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.persistence.common.model.AccessType.UPDATE
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.util.date.toStringDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.time.OffsetDateTime

@Import(FilePersistenceConfig::class)
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SubmissionDatesTest(
    @Autowired val securityTestService: SecurityTestService,
    @Autowired val submissionRepository: SubmissionPersistenceQueryService,
    @Autowired val toSubmissionMapper: ToSubmissionMapper,
    @LocalServerPort val serverPort: Int,
) {
    private lateinit var adminWebClient: BioWebClient
    private lateinit var userWebClient: BioWebClient

    @BeforeAll
    fun init(): Unit =
        runBlocking {
            securityTestService.ensureUserRegistration(DefaultUser)
            securityTestService.ensureUserRegistration(SuperUser)

            securityTestService.ensureSequence("S-BSST")

            userWebClient = getWebClient(serverPort, DefaultUser)
            adminWebClient = getWebClient(serverPort, SuperUser)
        }

    @Nested
    inner class DatesCases {
        @Test
        fun `28-1 Creation date is not changed beetween re submissions`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission", "CREATE-0010")
                        line("Title", "Sample Submission")
                        line()

                        line("Study")
                        line()
                    }.toString()

                assertThat(adminWebClient.submit(v1, TSV)).isSuccessful()
                val creationTime = submissionRepository.getExtByAccNoAndVersion("CREATE-0010", 1).creationTime

                val v2 =
                    tsv {
                        line("Submission", "CREATE-0010")
                        line("Title", "Sample Submission UPDATED")
                        line()

                        line("Study")
                        line()
                    }.toString()
                assertThat(adminWebClient.submit(v2, TSV)).isSuccessful()
                val v2Extended = submissionRepository.getExtByAccNoAndVersion("CREATE-0010", 2)
                assertThat(v2Extended.creationTime).isEqualTo(creationTime)
            }

        @Test
        fun `28-2 Modification date is changed beetween re submissions`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission", "MOD-0010")
                        line("Title", "Sample Submission")
                        line()

                        line("Study")
                        line()
                    }.toString()

                assertThat(adminWebClient.submit(v1, TSV)).isSuccessful()
                val modificationTime = submissionRepository.getExtByAccNoAndVersion("MOD-0010", 1).modificationTime

                val v2 =
                    tsv {
                        line("Submission", "MOD-0010")
                        line("Title", "Sample Submission UPDATED")
                        line()

                        line("Study")
                        line()
                    }.toString()
                assertThat(adminWebClient.submit(v2, TSV)).isSuccessful()
                val v2Extended = submissionRepository.getExtByAccNoAndVersion("MOD-0010", 2)
                assertThat(v2Extended.modificationTime).isAfter(modificationTime)
            }
    }

    @Nested
    inner class WhenRegularUser {
        @Test
        fun `28-3 Regular user submit with release date in the past`() =
            runTest {
                val submission =
                    tsv {
                        line("Submission")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2020-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()

                val exception = assertThrows<WebClientException> { userWebClient.submit(submission, TSV) }
                assertThat(exception).hasMessage(
                    """
                    {
                      "log": {
                        "level": "ERROR",
                        "message": "Submission validation errors",
                        "subnodes": [{
                          "level": "ERROR",
                          "message": "Release date cannot be in the past",
                          "subnodes": []
                        }]
                      },
                      "status": "FAIL"
                    }
                    """.trimIndent(),
                )
            }

        @Test
        fun `28-4 Regular user re-submit a public submission with a new release date in the future`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()

                        line("Study")
                        line()
                    }.toString()

                val submitted = userWebClient.submit(v1, TSV)
                assertThat(submitted).isSuccessful()

                val v2 =
                    tsv {
                        line("Submission", submitted.body.accNo)
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2040-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()

                val exception = assertThrows<WebClientException> { userWebClient.submit(v2, TSV) }
                assertThat(exception).hasMessage(
                    """
                    {
                      "log": {
                        "level": "ERROR",
                        "message": "Submission validation errors",
                        "subnodes": [{
                          "level": "ERROR",
                          "message": "The release date of a public study cannot be changed",
                          "subnodes": []
                        }]
                      },
                      "status": "FAIL"
                    }
                    """.trimIndent(),
                )
            }

        @Test
        fun `28-5 Regular user re-submit a private submission with a new release date in the past`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2010-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()

                val submitted = adminWebClient.submit(v1, TSV)
                assertThat(submitted).isSuccessful()

                val accNo = submitted.body.accNo
                val v2 =
                    tsv {
                        line("Submission", accNo)
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2011-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()

                adminWebClient.grantPermission(DefaultUser.email, accNo, UPDATE.name)
                val exception = assertThrows<WebClientException> { userWebClient.submit(v2, TSV) }
                assertThat(exception).hasMessage(
                    """
                    {
                      "log": {
                        "level": "ERROR",
                        "message": "Submission validation errors",
                        "subnodes": [{
                          "level": "ERROR",
                          "message": "The release date of a public study cannot be changed",
                          "subnodes": []
                        }]
                      },
                      "status": "FAIL"
                    }
                    """.trimIndent(),
                )
            }
    }

    @Nested
    inner class AdminUser {
        @Test
        fun `28-6 Admin submits and re submit in the past`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission", "RELEASE-0010")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2020-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()

                assertThat(adminWebClient.submit(v1, TSV)).isSuccessful()
                val v1Extended = submissionRepository.getExtByAccNoAndVersion("RELEASE-0010", 1)
                assertThat(v1Extended.releaseTime).isEqualTo(OffsetDateTime.parse("2020-04-24T00:00+00:00"))

                val v2 =
                    tsv {
                        line("Submission", "RELEASE-0010")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "1985-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()
                assertThat(adminWebClient.submit(v2, TSV)).isSuccessful()
                val v2Extended = submissionRepository.getExtByAccNoAndVersion("RELEASE-0010", 2)
                assertThat(v2Extended.releaseTime).isEqualTo(OffsetDateTime.parse("1985-04-24T00:00+00:00"))
            }

        @Test
        fun `28-7 Admin make a public submission private`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission", "RELEASE-0020")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2020-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()

                assertThat(adminWebClient.submit(v1, TSV)).isSuccessful()
                val v1Extended = submissionRepository.getExtByAccNoAndVersion("RELEASE-0020", 1)
                assertThat(v1Extended.released).isTrue()
                assertThat(v1Extended.releaseTime).isEqualTo(OffsetDateTime.parse("2020-04-24T00:00+00:00"))

                val v2 =
                    tsv {
                        line("Submission", "RELEASE-0020")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2050-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()
                assertThat(adminWebClient.submit(v2, TSV)).isSuccessful()
                val v2Extended = submissionRepository.getExtByAccNoAndVersion("RELEASE-0020", 2)
                assertThat(v2Extended.released).isFalse()
                assertThat(v2Extended.releaseTime).isEqualTo(OffsetDateTime.parse("2050-04-24T00:00+00:00"))
            }

        @Test
        fun `28-8 Admin make a Regular user public submission private`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()

                        line("Study")
                        line()
                    }.toString()

                val submitted = userWebClient.submit(v1, TSV)
                assertThat(submitted).isSuccessful()
                val accNo = submitted.body.accNo

                val v2 =
                    tsv {
                        line("Submission", accNo)
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2050-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()

                assertThat(adminWebClient.submit(v2, TSV)).isSuccessful()
                val v2Extended = submissionRepository.getExtByAccNoAndVersion(accNo, 2)
                assertThat(v2Extended.released).isFalse()
                assertThat(v2Extended.releaseTime).isEqualTo(OffsetDateTime.parse("2050-04-24T00:00+00:00"))
            }
    }

    @Nested
    inner class CollectionAdminUser {
        private val collectionAccNo = "PermissionCollection"
        private lateinit var colAdminWebClient: BioWebClient

        @BeforeAll
        fun beforeAll() =
            runBlocking {
                val collection =
                    tsv {
                        line("Submission", collectionAccNo)
                        line("AccNoTemplate", "!{S-PERMISIONT}")
                        line()

                        line("Project")
                    }.toString()
                assertThat(adminWebClient.submit(collection, TSV)).isSuccessful()

                securityTestService.ensureUserRegistration(CollectionUser)

                adminWebClient.grantPermission(CollectionUser.email, collectionAccNo, ADMIN.name)
                adminWebClient.grantPermission(DefaultUser.email, collectionAccNo, ATTACH.name)

                colAdminWebClient = getWebClient(serverPort, CollectionUser)
            }

        @BeforeEach
        fun beforeEach() {
            colAdminWebClient = getWebClient(serverPort, CollectionUser)
        }

        @Test
        fun `28-9 Collection Admin submits and re submit in the past`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2020-04-24")
                        line("AttachTo", collectionAccNo)
                        line()

                        line("Study")
                        line()
                    }.toString()

                val v1Submission = colAdminWebClient.submit(v1, TSV)
                val accNo = v1Submission.body.accNo
                assertThat(v1Submission).isSuccessful()

                val v1Extended = submissionRepository.getExtByAccNoAndVersion(accNo, 1)
                assertThat(v1Extended.releaseTime).isEqualTo(OffsetDateTime.parse("2020-04-24T00:00+00:00"))

                val v2 =
                    tsv {
                        line("Submission", accNo)
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "1985-04-24")
                        line("AttachTo", collectionAccNo)
                        line()

                        line("Study")
                        line()
                    }.toString()
                assertThat(colAdminWebClient.submit(v2, TSV)).isSuccessful()
                val v2Extended = submissionRepository.getExtByAccNoAndVersion(accNo, 2)
                assertThat(v2Extended.releaseTime).isEqualTo(OffsetDateTime.parse("1985-04-24T00:00+00:00"))
            }

        @Test
        fun `28-10 Collection Admin make a public submission private`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2020-04-24")
                        line("AttachTo", collectionAccNo)
                        line()

                        line("Study")
                        line()

                        line("File", "file_28-10.txt")
                        line()
                    }.toString()

                colAdminWebClient.uploadFile(tempFolder.createFile("file_28-10.txt", "28-10 content"))
                val v1Submission = colAdminWebClient.submit(v1, TSV)
                val accNo = v1Submission.body.accNo
                assertThat(v1Submission).isSuccessful()

                val v1Ext = submissionRepository.getExtByAccNoAndVersion(accNo, 1)
                assertThat(v1Ext.released).isTrue()
                assertThat(v1Ext.releaseTime).isEqualTo(OffsetDateTime.parse("2020-04-24T00:00+00:00"))

                val ftpFiles = FileUtils.listAllFiles(File("$ftpPath/${v1Ext.relPath}/Files"))
                val publishedFile = File("$ftpPath/${v1Ext.relPath}/Files/file_28-10.txt")
                assertThat(ftpFiles).containsExactly(publishedFile)
                assertThat(publishedFile).hasContent("28-10 content")
                assertThat(File("$submissionPath/${v1Ext.relPath}/Files/file_28-10.txt")).hasContent("28-10 content")

                val v2 =
                    tsv {
                        line("Submission", accNo)
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2050-04-24")
                        line("AttachTo", collectionAccNo)
                        line()

                        line("Study")
                        line()

                        line("File", "file_28-10.txt")
                        line()
                    }.toString()

                assertThat(colAdminWebClient.submit(v2, TSV)).isSuccessful()
                val v2Ext = submissionRepository.getExtByAccNoAndVersion(accNo, 2)
                assertThat(v2Ext.released).isFalse()
                assertThat(v2Ext.releaseTime).isEqualTo(OffsetDateTime.parse("2050-04-24T00:00+00:00"))

                val submissionFiles = FileUtils.listAllFiles(File("$submissionPath/${v2Ext.relPath}/Files"))
                val privateFile = File("$submissionPath/${v2Ext.relPath}/Files/file_28-10.txt")
                val unpublishedFile = File("$ftpPath/${v2Ext.relPath}/Files/file_28-10.txt")
                assertThat(unpublishedFile).doesNotExist()
                assertThat(submissionFiles).containsOnly(privateFile)
                assertThat(privateFile).hasContent("28-10 content")
            }

        @Test
        fun `28-11 Collection Admin make a Regular user public submission private`() =
            runTest {
                val v1 =
                    tsv {
                        line("Submission")
                        line("Title", "Sample Submission")
                        line("ReleaseDate", OffsetDateTime.now().toStringDate())
                        line()

                        line("Study")
                        line()
                    }.toString()

                val submitted = userWebClient.submit(v1, TSV)
                assertThat(submitted).isSuccessful()
                val accNo = submitted.body.accNo

                val v2 =
                    tsv {
                        line("Submission", accNo)
                        line("Title", "Sample Submission")
                        line("ReleaseDate", "2050-04-24")
                        line()

                        line("Study")
                        line()
                    }.toString()

                assertThat(adminWebClient.submit(v2, TSV)).isSuccessful()
                val v2Extended = submissionRepository.getExtByAccNoAndVersion(accNo, 2)
                assertThat(v2Extended.released).isFalse()
                assertThat(v2Extended.releaseTime).isEqualTo(OffsetDateTime.parse("2050-04-24T00:00+00:00"))
            }
    }
}
