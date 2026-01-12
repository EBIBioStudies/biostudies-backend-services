package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.migrations.ensureSubmissionIndexes
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocCollection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmissionRequest
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.createNfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds

@ExtendWith(SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
internal class SubmissionDocDataRepositoryTest(
    private val tempFolder: TemporaryFolder,
    @param:Autowired private val testInstance: SubmissionDocDataRepository,
    @param:Autowired private val requestDocDataRepository: SubmissionRequestDocDataRepository,
    @param:Autowired private val fileListDocFileRepo: FileListDocFileDocDataRepository,
    @param:Autowired private val mongoTemplate: ReactiveMongoTemplate,
) {
    @BeforeEach
    fun beforeEach() =
        runBlocking {
            testInstance.deleteAll()
            fileListDocFileRepo.deleteAll()
            mongoTemplate.ensureSubmissionIndexes()
        }

    @Nested
    inner class ExpireSubmissions {
        @Test
        fun `expire active processed versions`() =
            runTest {
                val referencedFile = tempFolder.createFile("referenced.txt")
                val file = createNfsFile("referenced.txt", "Files/referenced.txt", referencedFile)
                val fileListFile =
                    FileListDocFile(
                        id = ObjectId(),
                        submissionId = testDocSubmission.id,
                        file = file.toDocFile(),
                        fileListName = "file-list",
                        index = 1,
                        submissionVersion = 1,
                        submissionAccNo = "S-BSST4",
                    )

                testInstance.save(testDocSubmission.copy(accNo = "S-BSST4"))
                fileListDocFileRepo.save(fileListFile)

                testInstance.save(testDocSubmission.copy(accNo = "S-BSST4", version = 2))
                fileListDocFileRepo.save(fileListFile.copy(id = ObjectId(), submissionVersion = 2))

                testInstance.expireVersions(listOf("S-BSST4"))

                assertThat(testInstance.getByAccNoAndVersion("S-BSST4", version = -1)).isNotNull
                assertThat(testInstance.getByAccNoAndVersion("S-BSST4", version = -2)).isNotNull

                val r1 =
                    fileListDocFileRepo
                        .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName(
                            "S-BSST4",
                            0,
                            "file-list",
                        ).toList()
                assertThat(r1).isEmpty()

                val r2 =
                    fileListDocFileRepo
                        .findByFileList("S-BSST4", -1, "file-list")
                        .toList()
                assertThat(r2).hasSize(1)

                val r3 =
                    fileListDocFileRepo
                        .findByFileList("S-BSST4", -2, "file-list")
                        .toList()
                assertThat(r3).hasSize(1)
            }
    }

    @Nested
    inner class GetSubmissions {
        @Test
        fun `By email when is owner`() =
            runTest {
                testInstance.save(testDocSubmission.copy(accNo = "accNo1", owner = "anotherEmail"))
                testInstance.save(testDocSubmission.copy(accNo = "accNo2", version = -1, owner = "ownerEmail"))
                val d2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2", version = 2, owner = "ownerEmail"))

                val result = testInstance.getSubmissions(SubmissionListFilter(filterUser = "ownerEmail")).toList()

                assertThat(result).containsOnly(d2)
            }

        @Test
        fun `By AccNo When is not the owner`() =
            runTest {
                testInstance.save(testDocSubmission.copy(accNo = "accNo1"))
                val d2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2"))

                val result =
                    testInstance
                        .getSubmissions(SubmissionListFilter(OWNER, findAnyAccNo = true, accNo = "accNo2"))
                        .toList()

                assertThat(result).containsOnly(d2)
            }

        @Test
        fun `When collection admin`() =
            runTest {
                var sub =
                    testInstance.save(
                        testDocSubmission.copy(
                            accNo = "accNo1",
                            owner = "anotherEmail",
                            collections =
                                listOf(
                                    DocCollection("project"),
                                ),
                        ),
                    )

                val result =
                    testInstance
                        .getSubmissions(
                            SubmissionListFilter(
                                filterUser = "anotherUser",
                                adminCollections = listOf("project"),
                            ),
                        ).toList()
                assertThat(result).containsOnly(sub)
            }

        @Test
        fun `By AccNo When is the owner`() =
            runTest {
                val d1 = testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo1"))

                val result = testInstance.getSubmissions(SubmissionListFilter(OWNER, accNo = "accNo1")).toList()

                assertThat(result).containsOnly(d1)
            }

        @Test
        fun `When exclude particular accNo`() =
            runTest {
                testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo1"))

                val result =
                    testInstance.getSubmissions(SubmissionListFilter(OWNER, notIncludeAccNo = setOf("accNo1"))).toList()

                assertThat(result).isEmpty()
            }
    }

    @Nested
    inner class GetCurrentMaxVersion {
        @Test
        fun `by current version`() =
            runTest {
                testInstance.save(testDocSubmission.copy(accNo = "S-BSST5", version = -1))
                testInstance.save(testDocSubmission.copy(accNo = "S-BSST5", version = 2))

                assertThat(testInstance.getCurrentMaxVersion("S-BSST5")).isEqualTo(2)
            }

        @Test
        fun `by current version with all versions deleted`() =
            runTest {
                testInstance.save(testDocSubmission.copy(accNo = "S-BSST4", version = -1))
                testInstance.save(testDocSubmission.copy(accNo = "S-BSST4", version = -2))

                assertThat(testInstance.getCurrentMaxVersion2("S-BSST4")).isEqualTo(2)
            }

        @Test
        fun `get max version when both submission and request`() =
            runTest {
                testInstance.save(testDocSubmission.copy(accNo = "S-BSST5", version = -1))
                requestDocDataRepository.save(testDocSubmissionRequest.copy(accNo = "S-BSST5", version = 2))

                assertThat(testInstance.getCurrentMaxVersion2("S-BSST5")).isEqualTo(2)
            }
    }

    @Test
    fun getProjects() =
        runTest {
            testInstance.save(testDocSubmission)

            val projects = testInstance.getCollections(testDocSubmission.accNo)

            assertThat(projects).containsExactly(testDocCollection)
        }

    companion object {
        const val OWNER = "manuserager@ebi.ac.uk"

        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
