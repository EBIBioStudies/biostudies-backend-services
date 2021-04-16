package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import ebi.ac.uk.db.MONGO_VERSION
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
internal class SubmissionMongoQueryServiceTest(
    @MockK private val toExtSubmissionMapper: ToExtSubmissionMapper,
    @MockK private val serializationService: ExtSerializationService,
    @Autowired private val submissionRepo: SubmissionDocDataRepository,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository
) {
    private val testInstance =
        SubmissionMongoQueryService(submissionRepo, requestRepository, serializationService, toExtSubmissionMapper)

    @Test
    fun `expire submission`() {
        submissionRepo.save(testDocSubmission.copy(accNo = "S-BSST1", version = 1, status = PROCESSED))
        testInstance.expireSubmission("S-BSST1")

        assertThat(submissionRepo.findByAccNo("S-BSST1")).isNull()
    }

    @Test
    fun `expire non existing`() {
        val exception = assertThrows<SubmissionNotFoundException> { testInstance.expireSubmission("S-BSST2") }
        assertThat(exception.message).isEqualTo("The submission 'S-BSST2' was not found")
    }

    @Test
    fun `get non existing submission`() {
        val exception = assertThrows<SubmissionNotFoundException> { testInstance.getExtByAccNo("S-BSST3") }
        assertThat(exception.message).isEqualTo("The submission 'S-BSST3' was not found")
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
            register.add("app.persistence.enableMongo") { "true" }
        }
    }
}
