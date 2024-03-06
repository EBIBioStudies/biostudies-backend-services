package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
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
    @Autowired private val repository: FileListDocFileDocDataRepository,
) {

    @Test
    fun findBySubmissionAccNoAndSubmissionVersionAndFilePath() = runTest {
        val file = FireDocFile("filename", "filePath", "relPath", "fireId", true, listOf(), "md5", 1L, FILE.value)
        val fileListFile = FileListDocFile(
            id = ObjectId(),
            submissionId = ObjectId(),
            file = file,
            fileListName = "file-list",
            index = 0,
            submissionVersion = 1,
            submissionAccNo = "S-TEST123"
        )
        repository.save(fileListFile)

        val result = repository
            .findBySubmissionAccNoAndSubmissionVersionAndFilePath("S-TEST123", 1, "filePath")
            .toList()

        assertThat(result).containsExactly(fileListFile)
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
