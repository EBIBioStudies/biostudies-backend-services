package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_OWNER
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.util.collections.second
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.Duration.ofSeconds
import java.time.OffsetDateTime
import java.time.ZoneOffset
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED as REQUEST_PROCESSED

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
internal class SubmissionMongoQueryServiceTest(
    @MockK private val toExtSubmissionMapper: ToExtSubmissionMapper,
    @Autowired private val submissionRepo: SubmissionDocDataRepository,
    @Autowired private val requestRepository: SubmissionRequestDocDataRepository
) {
    private val serializationService: ExtSerializationService = ExtSerializationService()

    private val testInstance =
        SubmissionMongoQueryService(submissionRepo, requestRepository, serializationService, toExtSubmissionMapper)

    @Nested
    inner class ExpireSubmissions {
        @Test
        fun `expire submission`() {
            submissionRepo.save(testDocSubmission.copy(accNo = "S-BSST1", version = 1, status = PROCESSED))
            testInstance.expireSubmission("S-BSST1")

            assertThat(submissionRepo.findByAccNo("S-BSST1")).isNull()
        }

        @Test
        fun `expire submissions`() {
            submissionRepo.save(testDocSubmission.copy(accNo = "S-BSST1", version = 1, status = PROCESSED))
            submissionRepo.save(testDocSubmission.copy(accNo = "S-BSST101", version = 1, status = PROCESSED))
            testInstance.expireSubmissions(listOf("S-BSST1", "S-BSST101"))

            assertThat(submissionRepo.findByAccNo("S-BSST1")).isNull()
            assertThat(submissionRepo.findByAccNo("S-BSST101")).isNull()
        }
    }

    @Nested
    inner class GetSubmissionsByUser {
        private val section = rootSection.copy(fileList = null, files = listOf(), sections = listOf())

        @BeforeEach
        fun init() {
            requestRepository.deleteAll()
            submissionRepo.deleteAll()
        }

        @Test
        fun `filtered by accNo`() {
            val request1 = saveAsRequest(fullExtSubmission.copy(accNo = "accNo1", title = "title1", section = section), REQUESTED)
            val sub1 = submissionRepo.save(testDocSubmission.copy(accNo = "accNo1", status = PROCESSED))

            var result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(accNo = "accNo1", limit = 1)
            )

            assertThat(result).hasSize(1)
            assertThat(result.first()).isEqualTo(request1.asBasicSubmission())

            result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(accNo = "accNo1", limit = 2)
            )
            assertThat(result).hasSize(2)
            assertThat(result.first()).isEqualTo(request1.asBasicSubmission())
            assertThat(result.second()).isEqualTo(sub1.asBasicSubmission())
        }

        @Test
        fun `filtered by title`() {
            saveAsRequest(fullExtSubmission.copy(accNo = "acc1", title = "title", section = section), REQUESTED)
            saveAsRequest(fullExtSubmission.copy(accNo = "acc2", title = "wrongT1tl3", section = section), REQUESTED)
            submissionRepo.save(testDocSubmission.copy(accNo = "acc3", title = "title", status = PROCESSED))

            val result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(keywords = "title", limit = 2)
            )

            assertThat(result).hasSize(2)
            assertThat(result.first().accNo).isEqualTo("acc1")
            assertThat(result.second().accNo).isEqualTo("acc3")
        }

        @Test
        fun `filtered by type`() {
            val section1 = section.copy(type = "type1")
            val section2 = section.copy(type = "type2")
            val docSection1 = testDocSection.copy(type = "type1")

            saveAsRequest(fullExtSubmission.copy(accNo = "accNo1", section = section1), REQUESTED)
            saveAsRequest(fullExtSubmission.copy(accNo = "accNo2", section = section2), REQUESTED)
            submissionRepo.save(testDocSubmission.copy(accNo = "accNo3", status = PROCESSED, section = docSection1))

            val result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(type = "type1", limit = 2)
            )

            assertThat(result).hasSize(2)
            assertThat(result.first().accNo).isEqualTo("accNo1")
            assertThat(result.second().accNo).isEqualTo("accNo3")
        }

        @Test
        fun `filtered by from release time`() {
            val matchDate = OffsetDateTime.of(2010, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
            val mismatchDate = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

            saveAsRequest(
                fullExtSubmission.copy(
                    accNo = "accNo1", releaseTime = matchDate, section = section
                ),
                REQUESTED
            )
            saveAsRequest(
                fullExtSubmission.copy(
                    accNo = "accNo2", releaseTime = mismatchDate, section = section
                ),
                REQUESTED
            )
            submissionRepo.save(
                testDocSubmission.copy(accNo = "accNo3", releaseTime = matchDate.toInstant(), status = PROCESSED)
            )

            val result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(rTimeFrom = OffsetDateTime.of(2005, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), limit = 2)
            )

            assertThat(result).hasSize(2)
            assertThat(result.first().accNo).isEqualTo("accNo1")
            assertThat(result.second().accNo).isEqualTo("accNo3")
        }

        @Test
        fun `filtered by to release time`() {
            val matchDate = OffsetDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
            val mismatchDate = OffsetDateTime.of(2010, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

            saveAsRequest(
                fullExtSubmission.copy(
                    accNo = "accNo1", releaseTime = matchDate, section = section
                ),
                REQUESTED
            )
            saveAsRequest(
                fullExtSubmission.copy(
                    accNo = "accNo2", releaseTime = mismatchDate, section = section
                ),
                REQUESTED
            )
            submissionRepo.save(
                testDocSubmission.copy(accNo = "accNo3", releaseTime = matchDate.toInstant(), status = PROCESSED)
            )

            val result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(rTimeTo = OffsetDateTime.of(2005, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), limit = 3)
            )

            assertThat(result).hasSize(2)
            assertThat(result.first().accNo).isEqualTo("accNo1")
            assertThat(result.second().accNo).isEqualTo("accNo3")
        }

        @Test
        fun `filtered by released`() {
            saveAsRequest(fullExtSubmission.copy(accNo = "accNo1", released = true, section = section), REQUESTED)
            saveAsRequest(fullExtSubmission.copy(accNo = "accNo2", released = false, section = section), REQUESTED)
            submissionRepo.save(testDocSubmission.copy(accNo = "accNo3", released = true, status = PROCESSED))

            val result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(released = true, limit = 2)
            )

            assertThat(result).hasSize(2)
            assertThat(result.first().accNo).isEqualTo("accNo1")
            assertThat(result.second().accNo).isEqualTo("accNo3")
        }

        @Test
        fun `when all`() {
            saveAsRequest(
                fullExtSubmission.copy(
                    accNo = "accNo1",
                    version = 1,
                    title = "title",
                    status = ExtProcessingStatus.REQUESTED,
                    section = section.copy(type = "type1"),
                    released = false,
                    releaseTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
                ),
                REQUESTED
            )
            submissionRepo.save(
                testDocSubmission.copy(
                    accNo = "accNo1",
                    version = 1,
                    title = "title",
                    section = testDocSection.copy(type = "type1"),
                    released = false,
                    releaseTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                    status = PROCESSED
                )
            )

            val result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(
                    accNo = "accNo1",
                    version = 1,
                    type = "type1",
                    rTimeFrom = OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                    rTimeTo = OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                    keywords = "title",
                    released = false
                )
            )

            assertThat(result).hasSize(2)
            val request = result.first()
            assertThat(request.accNo).isEqualTo("accNo1")
            assertThat(request.version).isEqualTo(1)
            assertThat(request.title).isEqualTo("title")
            assertThat(request.releaseTime).isEqualTo(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            assertThat(request.released).isEqualTo(false)
            assertThat(request.status).isEqualTo(ProcessingStatus.REQUESTED)

            val submission = result.second()
            assertThat(submission.accNo).isEqualTo("accNo1")
            assertThat(submission.version).isEqualTo(1)
            assertThat(submission.title).isEqualTo("title")
            assertThat(submission.releaseTime).isEqualTo(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            assertThat(submission.released).isEqualTo(false)
            assertThat(submission.status).isEqualTo(ProcessingStatus.PROCESSED)
        }

        @Test
        fun `get greatest version submission`() {
            val sub1 = submissionRepo.save(testDocSubmission.copy(accNo = "accNo1", version = 3, status = PROCESSED))
            submissionRepo.save(testDocSubmission.copy(accNo = "accNo1", version = -2, status = PROCESSED))
            submissionRepo.save(testDocSubmission.copy(accNo = "accNo1", version = -1, status = PROCESSED))

            val result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(accNo = "accNo1", limit = 3)
            )

            assertThat(result).hasSize(1)
            assertThat(result.first()).isEqualTo(sub1.asBasicSubmission())
        }

        @Test
        fun `get only requests with status REQUESTED`() {
            saveAsRequest(fullExtSubmission.copy(accNo = "accNo1", title = "one", section = section), REQUESTED)
            saveAsRequest(fullExtSubmission.copy(accNo = "accNo1", title = "two", section = section), REQUEST_PROCESSED)
            saveAsRequest(
                fullExtSubmission.copy(accNo = "accNo1", title = "three", section = section),
                REQUEST_PROCESSED
            )

            val result = testInstance.getSubmissionsByUser(
                SUBMISSION_OWNER,
                SubmissionFilter(accNo = "accNo1", limit = 3)
            )

            assertThat(result).hasSize(1)
            assertThat(result.first().title).isEqualTo("one")
        }

        private fun saveAsRequest(extSubmission: ExtSubmission, status: SubmissionRequestStatus): ExtSubmission {
            requestRepository.saveRequest(asRequest(extSubmission, status))
            return extSubmission
        }

        private fun asRequest(submission: ExtSubmission, status: SubmissionRequestStatus) = SubmissionRequest(
            accNo = submission.accNo,
            version = submission.version,
            status = status,
            submission = BasicDBObject.parse(serializationService.serialize(submission))
        )
    }

    @Test
    fun `get non existing submission`() {
        val exception = assertThrows<SubmissionNotFoundException> { testInstance.getExtByAccNo("S-BSST3") }
        assertThat(exception.message).isEqualTo("The submission 'S-BSST3' was not found")
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
            register.add("app.persistence.enableMongo") { "true" }
        }
    }
}
