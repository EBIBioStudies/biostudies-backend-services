package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.commons.OffsetBasedPageRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtFileType.FILE
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
    @param:Autowired private val repository: FileListDocFileDocDataRepository,
) {
    @Test
    fun findBySubmissionAccNoAndSubmissionVersionAndFilePath() =
        runTest {
            val file =
                FireDocFile(
                    fileName = "filename",
                    filePath = "filePath",
                    relPath = "relPath",
                    fireId = "fireId",
                    attributes = listOf(),
                    md5 = "md5",
                    fileSize = 1L,
                    fileType = FILE.value,
                )
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

    @Test
    fun `find by file list removes identical duplicate entries`() =
        runTest {
            val fileListName = "file-list"
            val submissionAccNo = "S-TEST124"
            val duplicated =
                createFileListDocFile(submissionAccNo, fileListName, 0, "file-0", listOf(DocAttribute("GEN", "ABC")))
            val distinctMetadata =
                createFileListDocFile(submissionAccNo, fileListName, 2, "file-0", listOf(DocAttribute("GEN", "DEF")))
            val unique =
                createFileListDocFile(submissionAccNo, fileListName, 3, "file-2", listOf(DocAttribute("GEN", "GHI")))
            val duplicateWithDifferentStorageDetails =
                duplicated.copy(
                    id = ObjectId(),
                    index = 1,
                    file =
                        (duplicated.file as FireDocFile).copy(
                            fireId = "different-fire-id",
                            md5 = "different-md5",
                        ),
                )
            val files =
                listOf(
                    duplicated,
                    duplicateWithDifferentStorageDetails,
                    distinctMetadata,
                    unique,
                )
            repository.saveAll(files).toList()

            val result = repository.findByFileList(submissionAccNo, 1, fileListName).toList()

            assertThat(result).hasSize(3)
            assertThat(result.map { it.file.filePath }).containsExactlyInAnyOrder("file-0", "file-0", "file-2")
            assertThat(
                result
                    .filter { it.file.filePath == "file-0" }
                    .map {
                        it.file.attributes
                            .first()
                            .value
                    },
            ).containsExactlyInAnyOrder("ABC", "DEF")

            val page =
                repository.findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
                    submissionAccNo,
                    1,
                    fileListName,
                    OffsetBasedPageRequest.fromOffsetAndLimit(1, 1),
                )

            assertThat(page.content.map { it.file.filePath }).containsExactlyInAnyOrder("file-0")
            assertThat(page.totalElements).isEqualTo(4)
            assertThat(page.pageable.offset).isEqualTo(1)
        }

    private fun createFileListDocFile(
        submissionAccNo: String,
        fileListName: String,
        index: Int,
        filePath: String,
        attributes: List<DocAttribute> = listOf(),
    ) = FileListDocFile(
        id = ObjectId(),
        submissionId = ObjectId(),
        file =
            FireDocFile(
                fileName = "$filePath.txt",
                filePath = filePath,
                relPath = "relPath",
                fireId = "fireId",
                attributes = attributes,
                md5 = "md5",
                fileSize = 1L,
                fileType = FILE.value,
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
