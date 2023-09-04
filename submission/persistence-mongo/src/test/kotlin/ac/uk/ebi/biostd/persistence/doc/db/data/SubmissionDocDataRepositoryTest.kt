package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.repositories.getByAccNo
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocCollection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.createNfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds
import java.time.Instant.ofEpochSecond
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
internal class SubmissionDocDataRepositoryTest(
    private val tempFolder: TemporaryFolder,
    @Autowired private val testInstance: SubmissionDocDataRepository,
    @Autowired private val fileListDocFileRepo: FileListDocFileDocDataRepository,
) {
    @BeforeEach
    fun beforeEach() {
        testInstance.deleteAll()
        fileListDocFileRepo.deleteAll()
    }

    @Nested
    inner class ReleaseSubmission {
        @Test
        fun `release submission`() {
            testInstance.save(testDocSubmission.copy(accNo = "S-BIAD1", version = 1, released = false))
            testInstance.setAsReleased("S-BIAD1")

            assertThat(testInstance.getByAccNo(accNo = "S-BIAD1").released).isTrue
        }
    }

    @Nested
    inner class ExpireSubmissions {
        @Test
        fun `expire active processed versions`() {
            val referencedFile = tempFolder.createFile("referenced.txt")
            val file = createNfsFile("referenced.txt", "Files/referenced.txt", referencedFile)
            val fileListFile = FileListDocFile(
                id = ObjectId(),
                submissionId = testDocSubmission.id,
                file = file.toDocFile(),
                fileListName = "file-list",
                index = 1,
                submissionVersion = 1,
                submissionAccNo = "S-BSST4"
            )

            testInstance.save(testDocSubmission.copy(accNo = "S-BSST4"))
            fileListDocFileRepo.save(fileListFile)

            testInstance.save(testDocSubmission.copy(accNo = "S-BSST4", version = 2))
            fileListDocFileRepo.save(fileListFile.copy(id = ObjectId(), submissionVersion = 2))

            testInstance.expireVersions(listOf("S-BSST4"))

            assertThat(testInstance.getByAccNoAndVersion("S-BSST4", version = -1)).isNotNull
            assertThat(testInstance.getByAccNoAndVersion("S-BSST4", version = -2)).isNotNull
            assertThat(
                fileListDocFileRepo
                    .findAllBySubmissionAccNoAndSubmissionVersionGreaterThanAndFileListName("S-BSST4", 0, "file-list")
            ).isEmpty()
            assertThat(
                fileListDocFileRepo
                    .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName("S-BSST4", -1, "file-list")
            ).hasSize(1)
            assertThat(
                fileListDocFileRepo
                    .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName("S-BSST4", -2, "file-list")
            ).hasSize(1)
        }
    }

    @Nested
    inner class GetSubmissions {
        @Test
        fun `by email`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo1", owner = "anotherEmail"))
            val d2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2", owner = "ownerEmail"))

            val result = testInstance.getSubmissions(SubmissionFilter("ownerEmail"))

            assertThat(result).containsOnly(d2)
        }

        @Test
        fun `by type`() {
            testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo1"))
            val d2 = testInstance.save(
                testDocSubmission.copy(
                    owner = OWNER,
                    accNo = "accNo2",
                    section = testDocSection.copy(type = "work")
                )
            )

            val result = testInstance.getSubmissions(
                SubmissionFilter(OWNER, type = "work")
            )

            assertThat(result).containsOnly(d2)
        }

        @Test
        fun `by AccNo When is not the owner`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo1"))
            val d2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2"))

            val result = testInstance.getSubmissions(SubmissionFilter(OWNER, findAnyAccNo = true, accNo = "accNo2"))

            assertThat(result).containsOnly(d2)
        }

        @Test
        fun `by AccNo When is the owner`() {
            val d1 = testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo1"))

            val result = testInstance.getSubmissions(SubmissionFilter(OWNER, accNo = "accNo1"))

            assertThat(result).containsOnly(d1)
        }

        @Test
        fun `by release time`() {
            testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo1", releaseTime = ofEpochSecond(5)))
            val d2 = testInstance.save(
                testDocSubmission.copy(owner = OWNER, accNo = "accNo2", releaseTime = ofEpochSecond(15))
            )

            val result = testInstance.getSubmissions(
                SubmissionFilter(
                    OWNER,
                    rTimeFrom = OffsetDateTime.ofInstant(ofEpochSecond(10), ZoneOffset.UTC),
                    rTimeTo = OffsetDateTime.ofInstant(ofEpochSecond(20), ZoneOffset.UTC)
                )
            )

            assertThat(result).containsOnly(d2)
        }

        @Test
        fun `by keywords`() {
            testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo1", title = "another"))
            val d2 = testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo2", title = "title"))

            val result = testInstance.getSubmissions(SubmissionFilter(OWNER, keywords = "title"))

            assertThat(result).containsOnly(d2)
        }

        @Test
        fun `by released`() {
            testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo1", released = true))
            val d2 = testInstance.save(testDocSubmission.copy(owner = OWNER, accNo = "accNo2", released = false))

            val result = testInstance.getSubmissions(SubmissionFilter(OWNER, released = false))

            assertThat(result).containsOnly(d2)
        }

        @Test
        fun `by current version`() {
            testInstance.save(testDocSubmission.copy(accNo = "S-BSST3", version = -1))
            testInstance.save(testDocSubmission.copy(accNo = "S-BSST3", version = 2))

            assertThat(testInstance.getCurrentMaxVersion("S-BSST3")).isEqualTo(2)
        }

        @Test
        fun `by current version with all versions deleted`() {
            testInstance.save(testDocSubmission.copy(accNo = "S-BSST4", version = -1))
            testInstance.save(testDocSubmission.copy(accNo = "S-BSST4", version = -2))

            assertThat(testInstance.getCurrentMaxVersion("S-BSST4")).isEqualTo(2)
        }
    }

    @Test
    fun getProjects() {
        testInstance.save(testDocSubmission)

        val projects = testInstance.getCollections(testDocSubmission.accNo)

        assertThat(projects).containsExactly(testDocCollection)
    }

    companion object {
        const val OWNER = "manuserager@ebi.ac.uk"

        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
