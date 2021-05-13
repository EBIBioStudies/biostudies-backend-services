package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.CHANGE_LOG_COLLECTION
import ac.uk.ebi.biostd.persistence.doc.CHANGE_LOG_LOCK
import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig
import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig.Companion.createMongockConfig
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MONGO_VERSION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.createCollection
import org.springframework.data.mongodb.core.index.Index
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [MongoDbConfig::class])
@Testcontainers
internal class DatabaseChangeLogTest(
    @Autowired private val springContext: ApplicationContext,
    @Autowired private val mongoTemplate: MongoTemplate
) {
    @BeforeEach
    fun init() {
        mongoTemplate.dropCollection(DocSubmission::class.java)
        mongoTemplate.dropCollection(SubmissionRequest::class.java)
        mongoTemplate.dropCollection(CHANGE_LOG_COLLECTION)
        mongoTemplate.dropCollection(CHANGE_LOG_LOCK)
    }

    @Test
    fun `create schema migration 001 happy case`() {
        runMigrations()

        val submissionCollection = mongoTemplate.getCollectionName(DocSubmission::class.java)
        val requestCollection = mongoTemplate.getCollectionName(SubmissionRequest::class.java)

        assertSubmissionCollection(submissionCollection)
        assertRequestCollection(requestCollection)
    }

    @Test
    fun `create schema migration 001 when already exists`() {
        mongoTemplate.createCollection<DocSubmission>()
        mongoTemplate.createCollection<SubmissionRequest>()

        mongoTemplate.insert(testDocSubmission.copy(accNo = "accNo1"))
        mongoTemplate.insert(testDocSubmission.copy(accNo = "accNo2"))
        mongoTemplate.insert(request.copy(accNo = "accNo1"))
        mongoTemplate.insert(request.copy(accNo = "accNo2"))

        val submissions = mongoTemplate.findAll(DocSubmission::class.java)
        val requests = mongoTemplate.findAll(SubmissionRequest::class.java)

        runMigrations()

        val submissionCollection = mongoTemplate.getCollectionName(DocSubmission::class.java)
        val requestCollection = mongoTemplate.getCollectionName(SubmissionRequest::class.java)

        assertThat(mongoTemplate.findAll(DocSubmission::class.java)).isEqualTo(submissions)
        assertThat(mongoTemplate.findAll(SubmissionRequest::class.java)).isEqualTo(requests)
        assertSubmissionCollection(submissionCollection)
        assertRequestCollection(requestCollection)
    }

    private fun assertSubmissionCollection(submissionCollection: String) {
        val submissionIndexes = mongoTemplate.getCollection(submissionCollection).listIndexes().map { it["key"] }

        assertThat(mongoTemplate.collectionExists(submissionCollection)).isTrue()
        assertThat(mongoTemplate.getCollection(submissionCollection).listIndexes()).hasSize(7)

        assertThat(submissionIndexes).contains(Index().on("_id", ASC).indexKeys)
        assertThat(submissionIndexes).contains(Index().on("accNo", ASC).indexKeys)
        assertThat(submissionIndexes).contains(Index().on("accNo", ASC).on("version", ASC).indexKeys)
        assertThat(submissionIndexes).contains(Index().on("$SUB_SECTION.$SEC_TYPE", ASC).indexKeys)
        assertThat(submissionIndexes).contains(Index().on(SUB_RELEASE_TIME, ASC).indexKeys)
        assertThat(submissionIndexes).contains(Index().on(SUB_TITLE, ASC).indexKeys)
        assertThat(submissionIndexes).contains(Index().on(SUB_RELEASED, ASC).indexKeys)
    }

    private fun assertRequestCollection(requestCollection: String) {
        val requestIndexes = mongoTemplate.getCollection(requestCollection).listIndexes().map { it["key"] }

        assertThat(mongoTemplate.collectionExists(requestCollection)).isTrue()
        assertThat(mongoTemplate.getCollection(requestCollection).listIndexes()).hasSize(8)

        assertThat(requestIndexes).contains(Index().on("_id", ASC).indexKeys)
        assertThat(requestIndexes).contains(Index().on("accNo", ASC).indexKeys)
        assertThat(requestIndexes).contains(Index().on("accNo", ASC).on("version", ASC).indexKeys)
        assertThat(requestIndexes).contains(Index().on("submission.$SUB_SECTION.$SEC_TYPE", ASC).indexKeys)
        assertThat(requestIndexes).contains(Index().on("submission.$SUB_ACC_NO", ASC).indexKeys)
        assertThat(requestIndexes).contains(Index().on("submission.$SUB_RELEASE_TIME", ASC).indexKeys)
        assertThat(requestIndexes).contains(Index().on("submission.$SUB_TITLE", ASC).indexKeys)
        assertThat(requestIndexes).contains(Index().on("submission.$SUB_RELEASED", ASC).indexKeys)
    }

    private fun runMigrations() {
        val runner = createMongockConfig(mongoTemplate, springContext, "ac.uk.ebi.biostd.persistence.doc.migrations")
        runner.run(DefaultApplicationArguments())
    }

    private val request = SubmissionRequest(accNo = "accNo", version = 1, submission = BasicDBObject.parse("{}"))

    companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
            register.add("app.persistence.enableMongo") { "true" }
            register.add("app.mongo.execute-migrations") { "false" }
            register.add("app.mongo.migration-package") { "ac.uk.ebi.biostd.persistence.doc.migrations" }
        }
    }
}
