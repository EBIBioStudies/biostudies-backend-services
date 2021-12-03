package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.CHANGE_LOG_COLLECTION
import ac.uk.ebi.biostd.persistence.doc.CHANGE_LOG_LOCK
import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig
import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig.Companion.createMongockConfig
import ac.uk.ebi.biostd.persistence.doc.commons.getCollection
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import com.mongodb.BasicDBObject
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.model.constants.SectionFields.TITLE
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.collectionExists
import org.springframework.data.mongodb.core.createCollection
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.findAll
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds
import java.util.AbstractMap.SimpleEntry

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [MongoDbConfig::class])
@Testcontainers
internal class DatabaseChangeLogTest(
    @Autowired private val springContext: ApplicationContext,
    @Autowired private val mongoTemplate: MongoTemplate
) {
    @BeforeEach
    fun init() {
        mongoTemplate.dropCollection<DocSubmission>()
        mongoTemplate.dropCollection<SubmissionRequest>()
        mongoTemplate.dropCollection(CHANGE_LOG_COLLECTION)
        mongoTemplate.dropCollection(CHANGE_LOG_LOCK)
    }

    @Test
    fun `create schema migration 001 when collections does not exists`() {
        runMigrations()

        assertSubmissionCollection()
        assertRequestCollection()
    }

    @Test
    fun `create schema migration 001 when collections exists`() {
        mongoTemplate.createCollection<DocSubmission>()
        mongoTemplate.createCollection<SubmissionRequest>()

        mongoTemplate.insert(testDocSubmission.copy(accNo = "accNo1"))
        mongoTemplate.insert(testDocSubmission.copy(accNo = "accNo2"))
        mongoTemplate.insert(request.copy(id = ObjectId(), accNo = "accNo1"))
        mongoTemplate.insert(request.copy(id = ObjectId(), accNo = "accNo2"))

        val submissions = mongoTemplate.findAll<DocSubmission>()
        val requests = mongoTemplate.findAll<SubmissionRequest>()

        runMigrations()

        assertThat(mongoTemplate.findAll<DocSubmission>()).isEqualTo(submissions)
        assertThat(mongoTemplate.findAll<SubmissionRequest>()).isEqualTo(requests)
        assertSubmissionCollection()
        assertRequestCollection()
    }

    private fun assertSubmissionCollection() {
        val listIndexes = mongoTemplate.getCollection<DocSubmission>().listIndexes().toList()

        assertThat(mongoTemplate.collectionExists<DocSubmission>()).isTrue
        assertThat(listIndexes).hasSize(9)

        assertThat(listIndexes[0]).containsEntry("key", Document("_id", 1))
        assertThat(listIndexes[1]).containsEntry("key", Document(SUB_ACC_NO, 1))
        assertThat(listIndexes[2]).containsEntry("key", Document(SUB_ACC_NO, 1).append(SUB_VERSION, 1))
        assertThat(listIndexes[3]).containsEntry("key", Document(SUB_OWNER, 1))
        assertThat(listIndexes[4]).containsEntry("key", Document(SUB_SUBMITTER, 1))
        assertThat(listIndexes[5]).containsEntry("key", Document("$SUB_SECTION.$SEC_TYPE", 1))
        assertThat(listIndexes[6]).containsEntry("key", Document(SUB_RELEASE_TIME, 1))
        assertThat(listIndexes[7]).containsEntry("key", Document(SUB_RELEASED, 1))
        assertThat(listIndexes[8]).contains(
            SimpleEntry("textIndexVersion", 3),
            SimpleEntry("name", TITLE_INDEX_NAME),
            SimpleEntry("partialFilterExpression", Document("$SUB_SECTION.$SUB_ATTRIBUTES.name", TITLE.value)),
            SimpleEntry("weights", Document("$SUB_SECTION.$SUB_ATTRIBUTES.value", 1).append(SUB_TITLE, 1))
        )
    }

    private fun assertRequestCollection() {
        val listIndexes = mongoTemplate.getCollection<SubmissionRequest>().listIndexes().toList()

        assertThat(mongoTemplate.collectionExists<SubmissionRequest>()).isTrue
        assertThat(listIndexes).hasSize(10)

        assertThat(listIndexes[0]).containsEntry("key", Document("_id", 1))
        assertThat(listIndexes[1]).containsEntry("key", Document(SUB_ACC_NO, 1))
        assertThat(listIndexes[2]).containsEntry("key", Document(SUB_ACC_NO, 1).append(SUB_VERSION, 1))
        assertThat(listIndexes[3]).containsEntry("key", Document("submission.$SUB_SECTION.$SEC_TYPE", 1))
        assertThat(listIndexes[4]).containsEntry("key", Document("submission.$SUB_ACC_NO", 1))
        assertThat(listIndexes[5]).containsEntry("key", Document("submission.$SUB_OWNER", 1))
        assertThat(listIndexes[6]).containsEntry("key", Document("submission.$SUB_SUBMITTER", 1))
        assertThat(listIndexes[7]).containsEntry("key", Document("submission.$SUB_RELEASE_TIME", 1))
        assertThat(listIndexes[8]).containsEntry("key", Document("submission.$SUB_RELEASED", 1))
        assertThat(listIndexes[9]).contains(
            SimpleEntry("textIndexVersion", 3),
            SimpleEntry("name", TITLE_INDEX_NAME),
            SimpleEntry("partialFilterExpression", Document("$SUB.$SUB_SECTION.$SUB_ATTRIBUTES.name", TITLE.value)),
            SimpleEntry("weights", Document("$SUB.$SUB_SECTION.$SUB_ATTRIBUTES.value", 1).append("$SUB.$SUB_TITLE", 1))
        )
    }

    private fun runMigrations() {
        val runner = createMongockConfig(mongoTemplate, springContext, "ac.uk.ebi.biostd.persistence.doc.migrations")
        runner.run(DefaultApplicationArguments())
    }

    private val request = SubmissionRequest(
        accNo = "accNo",
        version = 1,
        status = REQUESTED,
        submission = BasicDBObject.parse("{}")
    )

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
            register.add("app.mongo.execute-migrations") { "false" }
            register.add("app.mongo.migration-package") { "ac.uk.ebi.biostd.persistence.doc.migrations" }
        }
    }
}
