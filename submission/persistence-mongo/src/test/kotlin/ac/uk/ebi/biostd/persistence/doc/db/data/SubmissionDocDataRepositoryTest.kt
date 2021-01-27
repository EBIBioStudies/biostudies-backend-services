package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSING
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MONGO_VERSION
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
            submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo10", version = 1, status = PROCESSING))

            testInstance.updateStatus(PROCESSED, "accNo10", 1)

            assertThat(submissionMongoRepository.getByAccNo(accNo = "accNo10").status).isEqualTo(PROCESSED)
        }

        @Test
        fun `status should not be updated when version does not match`() {
            submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo20", version = 1, status = PROCESSING))

            testInstance.updateStatus(PROCESSED, "accNo20", 3)

            assertThat(submissionMongoRepository.getByAccNo(accNo = "accNo20").status).isEqualTo(PROCESSING)
        }
    }

    @Test
    fun `successful status update`() {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo1", version = 1, status = PROCESSING))

        testInstance.updateStatus(PROCESSED, "accNo1", 1)

        assertThat(submissionMongoRepository.getByAccNo("accNo1").status).isEqualTo(PROCESSED)
    }

    @Test
    fun `status update when version does not match so do not update`() {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo2", version = 1, status = PROCESSING))

        testInstance.updateStatus(PROCESSED, "accNo2", 3)

        assertThat(submissionMongoRepository.getByAccNo(accNo = "accNo2").status).isEqualTo(PROCESSING)
    }

    @Test
    fun getCurrentVersion() {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo3", version = -1, status = PROCESSED))
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo3", version = 2, status = PROCESSED))

        assertThat(testInstance.getCurrentVersion("accNo3")).isEqualTo(2)
    }

    @Test
    fun expireActiveProcessedVersions() {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo4", version = -1, status = PROCESSED))
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo4", version = 2, status = PROCESSED))
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo4", version = 3, status = PROCESSING))

        testInstance.expireActiveProcessedVersions("accNo4")

        assertThat(submissionMongoRepository.getByAccNoAndVersion("accNo4", version = -1)).isNotNull
        assertThat(submissionMongoRepository.getByAccNoAndVersion("accNo4", version = -2)).isNotNull
        assertThat(submissionMongoRepository.getByAccNoAndVersion("accNo4", 3)).isNotNull
    }

    companion object {

        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("testDb") }
            register.add("spring.data.mongodb.database") { "testDb" }
            register.add("app.persistence.enableMongo") { "true" }
        }
    }
}
