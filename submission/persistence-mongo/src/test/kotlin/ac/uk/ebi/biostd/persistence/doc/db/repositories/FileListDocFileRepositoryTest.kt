package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.commons.OffsetBasedPageRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtFileType
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
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
import java.time.Duration

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class FileListDocFileRepositoryTest(
    @Autowired private val repository: FileListDocFileDocDataRepository,
) {
    @Test
    fun findBySubmissionAccNoAndSubmissionVersionAndFilePath() =
        runTest {
            val file = FireDocFile("filename", "filePath", "relPath", "fireId", listOf(), "md5", 1L, ExtFileType.FILE.value)
            val fileListFile =
                FileListDocFile(
                    id = ObjectId(),
                    submissionId = ObjectId(),
                    file = file,
                    fileListName = "file-list",
                    index = 0,
                    submissionVersion = 1,
                    submissionAccNo = "S-TEST123",
                )
            repository.save(fileListFile)

            val result =
                repository
                    .findBySubmissionAccNoAndSubmissionVersionAndFilePath("S-TEST123", 1, "filePath")
                    .toList()

            assertThat(result).containsExactly(fileListFile)
        }

    @Test
    fun `find all by submission and file list from index`() =
        runTest {
            val fileListName = "file-list"
            val submissionAccNo = "S-TEST123"
            val files =
                listOf(
                    createFileListDocFile(submissionAccNo, fileListName, 0, "file-0"),
                    createFileListDocFile(submissionAccNo, fileListName, 1, "file-1"),
                    createFileListDocFile(submissionAccNo, fileListName, 2, "file-2"),
                )
            repository.saveAll(files).toList()

            val page =
                repository.findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
                    submissionAccNo,
                    1,
                    fileListName,
                    OffsetBasedPageRequest.fromOffsetAndLimit(1, 2),
                )

            assertThat(page.content.map { it.file.filePath }).containsExactly("file-1", "file-2")
            assertThat(page.totalElements).isEqualTo(3)
            assertThat(page.pageable.offset).isEqualTo(1)
        }

    private fun createFileListDocFile(
        submissionAccNo: String,
        fileListName: String,
        index: Int,
        filePath: String,
    ) = FileListDocFile(
        id = ObjectId(),
        submissionId = ObjectId(),
        file =
            FireDocFile(
                fileName = "$filePath.txt",
                filePath = filePath,
                relPath = "relPath",
                fireId = "fireId",
                attributes = listOf(),
                md5 = "md5",
                fileSize = 1L,
                fileType = ExtFileType.FILE.value,
            ),
        fileListName = fileListName,
        index = index,
        submissionVersion = 1,
        submissionAccNo = submissionAccNo,
    )

    companion object {
        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
