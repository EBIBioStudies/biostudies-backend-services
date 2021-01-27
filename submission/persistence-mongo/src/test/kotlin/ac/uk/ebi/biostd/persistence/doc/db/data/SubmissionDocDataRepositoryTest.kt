package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSING
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Instant

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
internal class SubmissionDocDataRepositoryTest {

    @Autowired
    lateinit var testInstance: SubmissionDocDataRepository

    @Autowired
    lateinit var submissionMongoRepository: SubmissionMongoRepository

    @Nested
    inner class UpdateStatus {
        @Test
        fun `successful status update`() {
            submissionMongoRepository.save(testDocSubmission("accNo10", 1, PROCESSING))

            testInstance.updateStatus(PROCESSED, "accNo10", 1)

            assertThat(submissionMongoRepository.getByAccNo("accNo10").status).isEqualTo(PROCESSED)
        }

        @Test
        fun `status should not be updated when version does not match`() {
            submissionMongoRepository.save(testDocSubmission("accNo20", 1, PROCESSING))

            testInstance.updateStatus(PROCESSED, "accNo20", 3)

            assertThat(submissionMongoRepository.getByAccNo("accNo20").status).isEqualTo(PROCESSING)
        }
    }

    @Test
    fun `successful status update`() {
        submissionMongoRepository.save(testDocSubmission("accNo1", 1, PROCESSING))

        testInstance.updateStatus(PROCESSED, "accNo1", 1)

        assertThat(submissionMongoRepository.getByAccNo("accNo1").status).isEqualTo(PROCESSED)
    }

    @Test
    fun `status update when version does not match so do not update`() {
        submissionMongoRepository.save(testDocSubmission("accNo2", 1, PROCESSING))

        testInstance.updateStatus(PROCESSED, "accNo2", 3)

        assertThat(submissionMongoRepository.getByAccNo("accNo2").status).isEqualTo(PROCESSING)
    }

    @Test
    fun getCurrentVersion() {
        submissionMongoRepository.save(testDocSubmission("accNo3", -1, PROCESSED))
        submissionMongoRepository.save(testDocSubmission("accNo3", 2, PROCESSED))

        assertThat(testInstance.getCurrentVersion("accNo3")).isEqualTo(2)
    }

    @Test
    fun expireActiveProcessedVersions() {
        submissionMongoRepository.save(testDocSubmission("accNo4", -1, PROCESSED))
        submissionMongoRepository.save(testDocSubmission("accNo4", 2, PROCESSED))
        submissionMongoRepository.save(testDocSubmission("accNo4", 3, PROCESSING))

        testInstance.expireActiveProcessedVersions("accNo4")

        assertThat(submissionMongoRepository.getByAccNoAndVersion("accNo4", -1)).isNotNull
        assertThat(submissionMongoRepository.getByAccNoAndVersion("accNo4", -2)).isNotNull
        assertThat(submissionMongoRepository.getByAccNoAndVersion("accNo4", 3)).isNotNull
    }

    companion object {

        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("testDb") }
            register.add("spring.data.mongodb.database") { "testDb" }
            register.add("app.persistence.enableMongo") { "true" }
        }
    }

    private fun testDocSubmission(accNo: String, version: Int, status: DocProcessingStatus): DocSubmission {
        return DocSubmission(
            id = "",
            accNo = accNo,
            version = version,
            owner = "",
            submitter = "",
            title = "",
            method = DocSubmissionMethod.PAGE_TAB,
            relPath = "",
            rootPath = "",
            released = true,
            secretKey = "",
            status = status,
            releaseTime = Instant.ofEpochSecond(1),
            modificationTime = Instant.ofEpochSecond(1),
            creationTime = Instant.ofEpochSecond(1),
            section = DocSection(type = "Study"),
            attributes = listOf(),
            tags = listOf(),
            projects = listOf(),
            stats = listOf()
        )
    }
}
