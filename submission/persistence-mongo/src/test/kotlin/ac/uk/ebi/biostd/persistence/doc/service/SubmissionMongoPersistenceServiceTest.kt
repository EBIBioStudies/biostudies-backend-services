package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
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
import java.time.Duration

@ExtendWith(MockKExtension::class, SpringExtension::class, TemporaryFolderExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionMongoPersistenceServiceTest(
    @MockK private val submissionRepository: ExtSubmissionRepository,
    @Autowired private val subDataRepository: SubmissionDocDataRepository,
) {
    private val testInstance = SubmissionMongoPersistenceService(submissionRepository, subDataRepository)

    @AfterEach
    fun afterEach() = runBlocking {
        subDataRepository.deleteAllSubmissions()
    }

    @Nested
    inner class ExpireSubmissions {
        @Test
        fun `expire submission`() = runTest {
            subDataRepository.saveSubmission(testDocSubmission.copy(accNo = "S-BSST1", version = 1))
            testInstance.expireSubmission("S-BSST1")

            assertThat(subDataRepository.findByAccNo("S-BSST1")).isNull()
        }

        @Test
        fun `expire submissions`() = runTest {
            subDataRepository.save(testDocSubmission.copy(accNo = "S-BSST1", version = 1))
            subDataRepository.save(testDocSubmission.copy(accNo = "S-BSST101", version = 1))
            testInstance.expireSubmissions(listOf("S-BSST1", "S-BSST101"))

            assertThat(subDataRepository.findByAccNo("S-BSST1")).isNull()
            assertThat(subDataRepository.findByAccNo("S-BSST101")).isNull()
        }
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
