package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_METHOD
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_OWNER
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_RELEASED
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ROOT_PATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_SECRET_KEY
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_STATUS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_TITLE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.extCollection
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.extStat
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.extTag
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSectionAttribute
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSectionLink
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSectionTableLink
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.submissionAttribute
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import arrow.core.Either
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
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
    //@Autowired private val serializationService: ExtSerializationService,
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
        val rootSection2 = ExtSection(
            accNo = ROOT_SEC_ACC_NO,
            type = ROOT_SEC_TYPE,
            attributes = listOf(rootSectionAttribute),
            links = listOf(
                Either.left(rootSectionLink),
                Either.right(ExtLinkTable(links = listOf(rootSectionTableLink)))
            )
        )
        val fullExtSubmission2 = ExtSubmission(
            accNo = SUBMISSION_ACC_NO,
            version = SUBMISSION_VERSION,
            owner = SUBMISSION_OWNER,
            submitter = SUBMISSION_SUBMITTER,
            title = SUBMISSION_TITLE,
            method = SUBMISSION_METHOD,
            relPath = SUBMISSION_REL_PATH,
            rootPath = SUBMISSION_ROOT_PATH,
            released = SUBMISSION_RELEASED,
            secretKey = SUBMISSION_SECRET_KEY,
            status = SUBMISSION_STATUS,
            releaseTime = RELEASE_TIME,
            modificationTime = MODIFICATION_TIME,
            creationTime = CREATION_TIME,
            attributes = listOf(submissionAttribute),
            tags = listOf(extTag),
            collections = listOf(extCollection),
            section = rootSection2,
            stats = listOf(extStat)
        )

        requestRepository.saveRequest(asRequest(fullExtSubmission2.copy(accNo = "accNo2")))

        val result = testInstance.getSubmissionsByUser(SUBMISSION_OWNER, SubmissionFilter(accNo = "accNo2"))
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
