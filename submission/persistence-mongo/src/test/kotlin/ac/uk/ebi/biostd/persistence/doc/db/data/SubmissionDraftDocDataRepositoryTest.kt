package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.PROCESSING
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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
import java.time.Duration.ofSeconds
import kotlin.test.assertNotNull

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionDraftDocDataRepositoryTest(
    @Autowired val testInstance: SubmissionDraftDocDataRepository
) {
    private val testDocDraft = DocSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT, ACTIVE)
    private val testActiveDocDraft = DocSubmissionDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT, ACTIVE)
    private val testProcessingDocDraft = DocSubmissionDraft(USER_ID1, DRAFT_KEY1, DRAFT_CONTENT1, PROCESSING)

    @BeforeEach
    fun beforeEach() {
        testInstance.deleteAll()
    }

    @Test
    fun getSubmissionDraftByIdAndKey() {
        testInstance.save(testDocDraft)

        val result = testInstance.findByUserIdAndKey(USER_ID, DRAFT_KEY)

        assertThat(result).isEqualTo(testDocDraft)
    }

    @Test
    fun saveSubmissionDraft() {
        testInstance.saveDraft("user@test.org", "TMP_123", "{ type: 'submission' }")
        val saved = testInstance.findByUserIdAndKey("user@test.org", "TMP_123")

        assertNotNull(saved)
        assertThat(saved.content).isEqualTo("{ type: 'submission' }")
        assertThat(saved.status).isEqualTo(ACTIVE)
    }

    @Test
    fun updateSubmissionDraft() {
        testInstance.saveDraft("user@test.org", "TMP_124", "{ type: 'submission' }")
        testInstance.updateDraftContent("user@test.org", "TMP_124", "{ type: 'study' }")
        val updated = testInstance.findByUserIdAndKey("user@test.org", "TMP_124")

        assertNotNull(updated)
        assertThat(updated.content).isEqualTo("{ type: 'study' }")
    }

    @Test
    fun deleteSubmissionDraft() {
        testInstance.save(testDocDraft)

        assertThat(testInstance.findById(testDocDraft.id)).isNotNull()

        testInstance.deleteByUserIdAndKey(testDocDraft.userId, testDocDraft.key)

        assertThat(testInstance.findById(testDocDraft.id)).isEmpty()
    }

    @Test
    fun findAllByUserIdAndStatusDraft() {
        testInstance.save(testActiveDocDraft)
        testInstance.save(testProcessingDocDraft)

        val activeDrafts = testInstance.findAllByUserIdAndStatus(USER_ID, ACTIVE)
        assertThat(activeDrafts).hasSize(1)
        assertThat(activeDrafts.first()).isEqualTo(testActiveDocDraft)

        val processingDrafts = testInstance.findAllByUserIdAndStatus(USER_ID1, PROCESSING)
        assertThat(processingDrafts).hasSize(1)
        assertThat(processingDrafts.first()).isEqualTo(testProcessingDocDraft)

        testInstance.deleteByUserIdAndKey(testActiveDocDraft.userId, testActiveDocDraft.key)
        testInstance.deleteByUserIdAndKey(testProcessingDocDraft.userId, testProcessingDocDraft.key)

        assertThat(testInstance.findById(testActiveDocDraft.id)).isEmpty()
        assertThat(testInstance.findById(testProcessingDocDraft.id)).isEmpty()
    }

    @Test
    fun createSubmissionDraft() {
        val result = testInstance.createDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)

        assertThat(testInstance.getById(result.id)).isEqualTo(result)
        assertThat(result.status).isEqualTo(ACTIVE)
    }

    @Test
    fun setProcessingStatus() {
        testInstance.saveDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)

        val beforeChangeStatus = testInstance.findAll()
        assertThat(beforeChangeStatus).hasSize(1)
        assertThat(beforeChangeStatus.first().status).isEqualTo(ACTIVE)

        testInstance.setStatus(USER_ID, DRAFT_KEY, PROCESSING)

        val afterChangeStatus = testInstance.findAll()
        assertThat(afterChangeStatus).hasSize(1)
        assertThat(afterChangeStatus.first().status).isEqualTo(PROCESSING)
    }

    private companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }

        const val USER_ID = "jhon.doe@ebi.ac.uk"
        const val DRAFT_KEY = "key"
        const val DRAFT_CONTENT = "content"

        const val USER_ID1 = "jhon.doe1@ebi.ac.uk"
        const val DRAFT_KEY1 = "key1"
        const val DRAFT_CONTENT1 = "content1"
    }
}
