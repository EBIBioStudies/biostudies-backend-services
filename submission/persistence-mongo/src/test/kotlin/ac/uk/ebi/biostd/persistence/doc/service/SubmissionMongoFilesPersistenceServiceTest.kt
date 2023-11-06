package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbServicesConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.test.beans.TestConfig
import ac.uk.ebi.biostd.persistence.doc.test.doc.REL_PATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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

@ExtendWith(MockKExtension::class, SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class, MongoDbServicesConfig::class, TestConfig::class])
class SubmissionMongoFilesPersistenceServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val submission: ExtSubmission,
    @Autowired private val fileListDocFileRepository: FileListDocFileDocDataRepository,
) {
    private val testInstance = SubmissionMongoFilesPersistenceService(fileListDocFileRepository)

    @AfterEach
    fun afterEach(): Unit = runBlocking {
        fileListDocFileRepository.deleteAll()
    }

    private val nfsReferencedFile = tempFolder.createFile("nfsReferenced.txt")
    private val nfsFile = createNfsFile("nfsReferenced.txt", "Files/nfsReferenced.txt", nfsReferencedFile)
    private val nfsFileListFile = FileListDocFile(
        id = ObjectId(),
        submissionId = testDocSubmission.id,
        file = nfsFile.toDocFile(),
        fileListName = "test-file-list",
        index = 1,
        submissionVersion = testDocSubmission.version,
        submissionAccNo = testDocSubmission.accNo
    )
    private val fireReferencedFile = tempFolder.createFile("fireReferenced.txt")
    private val fireFile = FireFile(
        fireId = "fire-oid",
        firePath = "fire-path",
        published = false,
        filePath = "fireReferenced.txt",
        relPath = "Files/fireReferenced.txt",
        md5 = fireReferencedFile.md5(),
        size = fireReferencedFile.size(),
        type = FILE,
        attributes = emptyList()
    )
    private val fireFileListFile = FileListDocFile(
        ObjectId(),
        testDocSubmission.id,
        fireFile.toDocFile(),
        fileListName = "test-file-list",
        index = 2,
        submissionVersion = testDocSubmission.version,
        submissionAccNo = testDocSubmission.accNo
    )

    @BeforeEach
    fun beforeEach(): Unit = runBlocking {
        setUpMockSubmission()
        fileListDocFileRepository.save(nfsFileListFile)
        fileListDocFileRepository.save(fireFileListFile)
    }

    @Test
    fun `get referenced files`() = runTest {
        val files = testInstance.getReferencedFiles(submission, "test-file-list").toList()
        assertThat(files).hasSize(2)

        val nfsFile = files.first() as NfsFile
        assertThat(nfsFile.file).isEqualTo(nfsReferencedFile)
        assertThat(nfsFile.fullPath).isEqualTo(nfsReferencedFile.absolutePath)

        val fireFile = files.second() as FireFile
        assertThat(fireFile.fireId).isEqualTo("fire-oid")
        assertThat(fireFile.filePath).isEqualTo("fireReferenced.txt")
        assertThat(fireFile.relPath).isEqualTo("Files/fireReferenced.txt")
        assertThat(fireFile.firePath).isEqualTo("${submission.relPath}/Files/fireReferenced.txt")
    }

    @Test
    fun `non existing referenced files`() = runTest {
        val result = testInstance.getReferencedFiles(submission, "non-existing-fileListName")

        assertThat(result.toList()).hasSize(0)
    }

    private fun setUpMockSubmission() {
        every { submission.accNo } returns SUB_ACC_NO
        every { submission.relPath } returns REL_PATH
        every { submission.released } returns false
    }

    companion object {
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
