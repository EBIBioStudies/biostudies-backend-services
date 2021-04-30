package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_OWNER
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
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
    @Autowired private val submissionRepo: SubmissionDocDataRepository,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository
) {
    private val testInstance =
        SubmissionMongoQueryService(submissionRepo, requestRepository, ExtSerializationService(), toExtSubmissionMapper)

    @Test
    fun `expire submission`() {
        submissionRepo.save(testDocSubmission.copy(accNo = "S-BSST1", version = 1, status = PROCESSED))
        testInstance.expireSubmission("S-BSST1")

        assertThat(submissionRepo.findByAccNo("S-BSST1")).isNull()
    }

    @Test
    fun getSubmissionsByUser() {
        val sectionWithoutFiles = rootSection.copy(fileList = null, files = listOf(), sections = listOf())
        val request1 = fullExtSubmission.copy(accNo = "accNo1", title = "title", section = sectionWithoutFiles)
        val request2 = fullExtSubmission.copy(accNo = "accNo2", title = "title", section = sectionWithoutFiles)
        val request3 = fullExtSubmission.copy(accNo = "accNo3", title = "wrongT1tl3", section = sectionWithoutFiles)
        val submission1 = testDocSubmission.copy(accNo = "accNo1", title = "title", status = PROCESSED)
        val submission2 = testDocSubmission.copy(accNo = "accNo2", title = "title", status = PROCESSED)
        val submission3 = testDocSubmission.copy(accNo = "accNo3", title = "wrongT1tl3", status = PROCESSED)

        requestRepository.saveRequest(asRequest(request1))
        requestRepository.saveRequest(asRequest(request2))
        requestRepository.saveRequest(asRequest(request3))
        submissionRepo.save(submission1)
        submissionRepo.save(submission2)
        submissionRepo.save(submission3)

        val result = testInstance.getSubmissionsByUser(
            SUBMISSION_OWNER,
            SubmissionFilter(keywords = "title", limit = 3)
        )

        assertThat(result).hasSize(3)
        assertThat(result.first()).isEqualTo(request1.asBasicSubmission())
        assertThat(result.second()).isEqualTo(request2.asBasicSubmission())
        assertThat(result.third()).isEqualTo(submission1.asBasicSubmission())
    }

    private fun asRequest(submission: ExtSubmission) = SubmissionRequest(
        accNo = submission.accNo,
        version = submission.version,
        submission = BasicDBObject.parse(ExtSerializationService().serialize(submission))
    )

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
