package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSING
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocProject
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MONGO_VERSION
import org.assertj.core.api.Assertions.assertThat
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
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
internal class SubmissionDocDataRepositoryTest {

    @Autowired
    lateinit var testInstance: SubmissionDocDataRepository

    @BeforeEach
    fun beforeEach() {
        testInstance.deleteAll()
    }

    @Nested
    inner class UpdateStatus {
        @Test
        fun `successful status update`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo10", version = 1, status = PROCESSING))

            testInstance.updateStatus(PROCESSED, "accNo10", 1)

            assertThat(testInstance.getByAccNo(accNo = "accNo10").status).isEqualTo(PROCESSED)
        }

        @Test
        fun `status should not be updated when version does not match`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo20", version = 1, status = PROCESSING))

            testInstance.updateStatus(PROCESSED, "accNo20", 3)

            assertThat(testInstance.getByAccNo(accNo = "accNo20").status).isEqualTo(PROCESSING)
        }
    }

    @Test
    fun getCurrentVersion() {
        testInstance.save(testDocSubmission.copy(accNo = "accNo3", version = -1, status = PROCESSED))
        testInstance.save(testDocSubmission.copy(accNo = "accNo3", version = 2, status = PROCESSED))

        assertThat(testInstance.getCurrentVersion("accNo3")).isEqualTo(2)
    }

    @Test
    fun expireActiveProcessedVersions() {
        testInstance.save(testDocSubmission.copy(accNo = "accNo4", version = -1, status = PROCESSED))
        testInstance.save(testDocSubmission.copy(accNo = "accNo4", version = 2, status = PROCESSED))
        testInstance.save(testDocSubmission.copy(accNo = "accNo4", version = 3, status = PROCESSING))

        testInstance.expireActiveProcessedVersions("accNo4")

        assertThat(testInstance.getByAccNoAndVersion("accNo4", version = -1)).isNotNull
        assertThat(testInstance.getByAccNoAndVersion("accNo4", version = -2)).isNotNull
        assertThat(testInstance.getByAccNoAndVersion("accNo4", 3)).isNotNull
    }

    @Test
    fun getProjects() {
        testInstance.save(testDocSubmission)

        val projects = testInstance.getProjects(testDocSubmission.accNo)

        assertThat(projects).containsExactly(testDocProject)
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
