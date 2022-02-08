package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.CHANGE_LOG_COLLECTION
import ac.uk.ebi.biostd.persistence.doc.CHANGE_LOG_LOCK
import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig
import ac.uk.ebi.biostd.persistence.doc.MongoDbConfig.Companion.createMongockConfig
import ac.uk.ebi.biostd.persistence.doc.commons.collection
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE_LIST_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.CONTENT
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.KEY
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.STATUS
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.USER_ID
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.model.constants.SectionFields.TITLE
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
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
        mongoTemplate.dropCollection<DocSubmissionRequest>()
        mongoTemplate.dropCollection<DocSubmissionDraft>()
        mongoTemplate.dropCollection<FileListDocFile>()

        mongoTemplate.dropCollection(CHANGE_LOG_COLLECTION)
        mongoTemplate.dropCollection(CHANGE_LOG_LOCK)
    }

    @Test
    fun `run migration 001`() {
        fun assertSubmissionIndexes() {
            val submissionIndexes = mongoTemplate.collection<DocSubmission>().listIndexes().toList()

            assertThat(mongoTemplate.collectionExists<DocSubmission>()).isTrue()
            assertThat(submissionIndexes).hasSize(9)

            assertThat(submissionIndexes[0]).containsEntry("key", Document("_id", 1))
            assertThat(submissionIndexes[1]).containsEntry("key", Document(SUB_ACC_NO, 1))
            assertThat(submissionIndexes[2]).containsEntry("key", Document(SUB_ACC_NO, 1).append(SUB_VERSION, 1))
            assertThat(submissionIndexes[3]).containsEntry("key", Document(SUB_OWNER, 1))
            assertThat(submissionIndexes[4]).containsEntry("key", Document(SUB_SUBMITTER, 1))
            assertThat(submissionIndexes[5]).containsEntry("key", Document("$SUB_SECTION.$SEC_TYPE", 1))
            assertThat(submissionIndexes[6]).containsEntry("key", Document(SUB_RELEASE_TIME, 1))
            assertThat(submissionIndexes[8]).containsEntry("key", Document(SUB_RELEASED, 1))
            assertThat(submissionIndexes[7]).contains(
                SimpleEntry("textIndexVersion", 3),
                SimpleEntry("name", TITLE_INDEX_NAME),
                SimpleEntry("weights", Document(SUB_TITLE, 1))
            )
        }

        fun assertRequestIndexes() {
            val requestIndexes = mongoTemplate.collection<DocSubmissionRequest>().listIndexes().toList()
            assertThat(mongoTemplate.collectionExists<DocSubmissionRequest>()).isTrue()
            assertThat(requestIndexes).hasSize(10)
            assertThat(requestIndexes[0]).containsEntry("key", Document("_id", 1))
            assertThat(requestIndexes[1]).containsEntry("key", Document(SUB_ACC_NO, 1))
            assertThat(requestIndexes[2]).containsEntry("key", Document(SUB_ACC_NO, 1).append(SUB_VERSION, 1))
            assertThat(requestIndexes[3]).containsEntry("key", Document("$SUB.$SUB_SECTION.$SEC_TYPE", 1))
            assertThat(requestIndexes[4]).containsEntry("key", Document("$SUB.$SUB_ACC_NO", 1))
            assertThat(requestIndexes[5]).containsEntry("key", Document("$SUB.$SUB_OWNER", 1))
            assertThat(requestIndexes[6]).containsEntry("key", Document("$SUB.$SUB_SUBMITTER", 1))
            assertThat(requestIndexes[7]).containsEntry("key", Document("$SUB.$SUB_RELEASE_TIME", 1))
            assertThat(requestIndexes[9]).containsEntry("key", Document("$SUB.$SUB_RELEASED", 1))
            assertThat(requestIndexes[8]).contains(
                SimpleEntry("textIndexVersion", 3),
                SimpleEntry("name", TITLE_INDEX_NAME),
                SimpleEntry("weights", Document("$SUB.$SUB_TITLE", 1))
            )
        }

        runMigrations(ChangeLog001::class.java)

        assertSubmissionIndexes()
        assertRequestIndexes()
    }

    @Test
    fun `run migration 002`() {
        fun assertTitleTextIndexes() {
            val docSubmissionIndexes = mongoTemplate.collection<DocSubmission>().listIndexes().toList()

            assertThat(mongoTemplate.collectionExists<DocSubmission>()).isTrue()
            assertThat(docSubmissionIndexes).hasSize(2)
            assertThat(docSubmissionIndexes.first()).containsEntry("key", Document("_id", 1))
            assertThat(docSubmissionIndexes.second()).contains(
                SimpleEntry("textIndexVersion", 3),
                SimpleEntry("name", TITLE_INDEX_NAME),
                SimpleEntry("partialFilterExpression", Document("$SUB_SECTION.$SUB_ATTRIBUTES.name", TITLE.value)),
                SimpleEntry("weights", Document("$SUB_SECTION.$SUB_ATTRIBUTES.value", 1).append(SUB_TITLE, 1))
            )

            val docSubRequestIndexes = mongoTemplate.collection<DocSubmissionRequest>().listIndexes().toList()

            assertThat(mongoTemplate.collectionExists<DocSubmissionRequest>()).isTrue()
            assertThat(docSubRequestIndexes).hasSize(2)
            assertThat(docSubRequestIndexes.first()).containsEntry("key", Document("_id", 1))
            assertThat(docSubRequestIndexes.second()).contains(
                SimpleEntry("textIndexVersion", 3),
                SimpleEntry("name", TITLE_INDEX_NAME),
                SimpleEntry("partialFilterExpression", Document("$SUB.$SUB_SECTION.$SUB_ATTRIBUTES.name", TITLE.value)),
                SimpleEntry(
                    "weights",
                    Document("$SUB.$SUB_SECTION.$SUB_ATTRIBUTES.value", 1).append("$SUB.$SUB_TITLE", 1)
                )
            )
        }

        runMigrations(ChangeLog002::class.java)

        assertTitleTextIndexes()
    }

    @Test
    fun `run migration 003`() {
        fun assertModificationTimeIndexes() {
            val docSubmissionIndexes = mongoTemplate.collection<DocSubmission>().listIndexes().toList()

            assertThat(mongoTemplate.collectionExists<DocSubmission>()).isTrue()
            assertThat(docSubmissionIndexes).hasSize(2)
            assertThat(docSubmissionIndexes.first()).containsEntry("key", Document("_id", 1))
            assertThat(docSubmissionIndexes.second()).containsEntry("key", Document(SUB_MODIFICATION_TIME, -1))

            val docSubRequestIndexes = mongoTemplate.collection<DocSubmissionRequest>().listIndexes().toList()

            assertThat(mongoTemplate.collectionExists<DocSubmissionRequest>()).isTrue()
            assertThat(docSubRequestIndexes).hasSize(2)
            assertThat(docSubRequestIndexes.first()).containsEntry("key", Document("_id", 1))
            assertThat(docSubRequestIndexes.second()).containsEntry("key", Document("$SUB.$SUB_MODIFICATION_TIME", -1))
        }

        runMigrations(ChangeLog003::class.java)

        assertModificationTimeIndexes()
    }

    @Test
    fun `run migration 004`() {
        fun assertFileListIndexes() {
            val listIndexes = mongoTemplate.collection<FileListDocFile>().listIndexes().toList()
            assertThat(mongoTemplate.collectionExists<FileListDocFile>()).isTrue()
            assertThat(listIndexes).hasSize(6)
            assertThat(listIndexes[0]).containsEntry("key", Document("_id", 1))
            assertThat(listIndexes[1]).containsEntry("key", Document(FILE_LIST_DOC_FILE_SUBMISSION_ID, 1))
            assertThat(listIndexes[2]).containsEntry("key", Document(FILE_LIST_DOC_FILE_FILE_LIST_NAME, 1))
            assertThat(listIndexes[3]).containsEntry("key", Document(FILE_LIST_DOC_FILE_INDEX, 1))
            assertThat(listIndexes[4]).containsEntry("key", Document(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, 1))
            assertThat(listIndexes[5]).containsEntry("key", Document(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, 1))
        }

        runMigrations(ChangeLog004::class.java)

        assertFileListIndexes()
    }

    @Test
    fun `run migration 005`() {
        val docDraft = Document(mapOf(USER_ID to "owner@email.org", KEY to "draftKey", CONTENT to "draftContent"))
        val draftCollection = mongoTemplate.createCollection<DocSubmissionDraft>()
        mongoTemplate.insert(docDraft, draftCollection.namespace.collectionName)

        val drafts = mongoTemplate.findAll<Document>(draftCollection.namespace.collectionName)
        assertThat(drafts).hasSize(1)
        assertThat(drafts.first()[STATUS]).isNull()

        runMigrations(ChangeLog005::class.java)

        val draftsAfterMigrations = mongoTemplate.findAll<DocSubmissionDraft>(draftCollection.namespace.collectionName)
        assertThat(draftsAfterMigrations).hasSize(1)
        assertThat(draftsAfterMigrations.first().status).isEqualTo(DocSubmissionDraft.DraftStatus.ACTIVE)
    }

    private fun runMigrations(clazz: Class<*>) {
        val runner = createMongockConfig(mongoTemplate, springContext, listOf(clazz))
        runner.run(DefaultApplicationArguments())
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
            register.add("app.mongo.execute-migrations") { "false" }
            register.add("app.mongo.migration-package") { "ac.uk.ebi.biostd.persistence.doc.migrations" }
        }
    }
}
