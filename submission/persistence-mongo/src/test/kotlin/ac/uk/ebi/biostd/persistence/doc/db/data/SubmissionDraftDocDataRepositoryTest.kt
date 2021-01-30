package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.test.doc.DRAFT_CONTENT
import ac.uk.ebi.biostd.persistence.doc.test.doc.DRAFT_KEY
import ac.uk.ebi.biostd.persistence.doc.test.doc.USER_ID
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocDraft
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
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionDraftDocDataRepositoryTest(
    @Autowired val testInstance: SubmissionDraftDocDataRepository
) {

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
        testInstance.save(testDocDraft)

        assertThat(testInstance.getById(testDocDraft.id)).isEqualTo(testDocDraft)
    }

    @Test
    fun deleteSubmissionDraft() {
        testInstance.save(testDocDraft)

        assertThat(testInstance.findById(testDocDraft.id)).isNotNull()

        testInstance.deleteByUserIdAndKey(testDocDraft.userId, testDocDraft.key)

        assertThat(testInstance.findById(testDocDraft.id)).isEmpty()
    }

    @Test
    fun createSubmissionDraft() {
        val result = testInstance.createDraft(USER_ID, DRAFT_KEY, DRAFT_CONTENT)

        assertThat(testInstance.getById(result.id)).isEqualTo(result)
    }

    @Test
    fun findAllByUserId() {
        val anotherDoc = DocSubmissionDraft(USER_ID, "another-key", "anotherContent")

        testInstance.save(testDocDraft)
        testInstance.save(anotherDoc)

        val result = testInstance.findAllByUserId(USER_ID).sortedBy { it.key }

        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(anotherDoc)
        assertThat(result[1]).isEqualTo(testDocDraft)
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
