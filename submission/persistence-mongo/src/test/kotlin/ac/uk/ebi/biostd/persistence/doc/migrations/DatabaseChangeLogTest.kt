package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.MongoDbReactiveConfig
import ac.uk.ebi.biostd.persistence.doc.commons.collection
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.COLLECTION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.STORAGE_MODE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE_LIST_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.collectionExists
import org.springframework.data.mongodb.core.dropCollection
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
@SpringBootTest(classes = [MongoDbReactiveConfig::class])
@Testcontainers
internal class DatabaseChangeLogTest(
    @Autowired private val springContext: ApplicationContext,
    @Autowired private val mongoTemplate: ReactiveMongoTemplate,
) {
    @BeforeEach
    fun init() {
        mongoTemplate.dropCollection<DocSubmission>()
        mongoTemplate.dropCollection<DocSubmissionRequest>()
        mongoTemplate.dropCollection<DocSubmissionRequestFile>()
        mongoTemplate.dropCollection<DocSubmissionDraft>()
        mongoTemplate.dropCollection<FileListDocFile>()
    }

    @Test
    fun `run migration 001`() =
        runTest {
            fun assertSubmissionCoreIndexes(
                prefix: String = EMPTY,
                indexes: List<Document>,
            ) {
                assertThat(indexes[1]).containsEntry("key", Document("$prefix$SUB_ACC_NO", 1))
                assertThat(indexes[2]).containsEntry("key", Document("$prefix$SUB_ACC_NO", 1).append(SUB_VERSION, 1))
                assertThat(indexes[3]).containsEntry("key", Document("$prefix$SUB_OWNER", 1))
                assertThat(indexes[4]).containsEntry("key", Document("$prefix$SUB_SUBMITTER", 1))
                assertThat(indexes[5]).containsEntry("key", Document("$prefix$SUB_SECTION.$SEC_TYPE", 1))
                assertThat(indexes[6]).containsEntry("key", Document("$prefix$SUB_RELEASE_TIME", 1))
                assertThat(indexes[7]).containsEntry("key", Document("$prefix$SUB_RELEASED", 1))
                assertThat(indexes[8]).containsEntry("key", Document("$prefix$SUB_MODIFICATION_TIME", 1))
                assertThat(indexes[9]).containsEntry(
                    "key",
                    Document("$prefix$SUB_COLLECTIONS.$COLLECTION_ACC_NO", 1).append(SUB_VERSION, 1),
                )
                assertThat(indexes[10]).containsEntry(
                    "key",
                    Document("$prefix$SUB_COLLECTIONS.$COLLECTION_ACC_NO", 1)
                        .append("$prefix$SUB_VERSION", 1)
                        .append("$prefix$STORAGE_MODE", 1),
                )
                assertThat(indexes[11]).contains(
                    SimpleEntry(
                        "weights",
                        Document("$prefix$SUB_SECTION.$SEC_ATTRIBUTES.$ATTRIBUTE_DOC_NAME", 1)
                            .append("$prefix$SUB_SECTION.$SEC_ATTRIBUTES.$ATTRIBUTE_DOC_VALUE", 1)
                            .append("$prefix$SUB_TITLE", 1),
                    ),
                )
            }

            suspend fun assertSubmissionIndexes() {
                val submissionIndexes = mongoTemplate.collection<DocSubmission>().listIndexes().asFlow().toList()

                assertThat(mongoTemplate.collectionExists<DocSubmission>().awaitSingle()).isTrue()
                assertThat(submissionIndexes).hasSize(12)

                assertThat(submissionIndexes[0]).containsEntry("key", Document("_id", 1))
                assertSubmissionCoreIndexes(indexes = submissionIndexes)
            }

            suspend fun assertRequestIndexes() {
                val requestIndexes = mongoTemplate.collection<DocSubmissionRequest>().listIndexes().asFlow().toList()
                assertThat(mongoTemplate.collectionExists<DocSubmissionRequest>().awaitSingle()).isTrue()
                assertThat(requestIndexes).hasSize(14)

                assertThat(requestIndexes[0]).containsEntry("key", Document("_id", 1))
                assertSubmissionCoreIndexes("$SUB.", indexes = requestIndexes)
                assertThat(requestIndexes[12]).containsEntry("key", Document(SUB_ACC_NO, 1))
                assertThat(requestIndexes[13]).containsEntry("key", Document(SUB_ACC_NO, 1).append(SUB_VERSION, 1))
            }

            suspend fun assertFileListIndexes() {
                val listIndexes = mongoTemplate.collection<FileListDocFile>().listIndexes().asFlow().toList()
                assertThat(mongoTemplate.collectionExists<FileListDocFile>().awaitSingle()).isTrue()
                assertThat(listIndexes).hasSize(8)
                assertThat(listIndexes[0]).containsEntry("key", Document("_id", 1))
                assertThat(listIndexes[1]).containsEntry("key", Document(FILE_LIST_DOC_FILE_SUBMISSION_ID, 1))
                assertThat(listIndexes[3]).containsEntry("key", Document(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, 1))
                assertThat(listIndexes[4]).containsEntry("key", Document(FILE_LIST_DOC_FILE_FILE_LIST_NAME, 1))
                assertThat(listIndexes[5]).containsEntry("key", Document(FILE_LIST_DOC_FILE_INDEX, 1))
                assertThat(listIndexes[6]).containsEntry("key", Document(FILE_DOC_FILEPATH, 1))
                assertThat(listIndexes[7]).containsEntry(
                    "key",
                    Document(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, 1)
                        .append(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, 1)
                        .append("$FILE_LIST_DOC_FILE_FILE.$FILE_DOC_FILEPATH", 1),
                )
            }

            suspend fun assertStatsIndexes() {
                val statsIndexes = mongoTemplate.collection<DocSubmissionStats>().listIndexes().asFlow().toList()
                assertThat(statsIndexes).hasSize(2)
                assertThat(statsIndexes[0]).containsEntry("key", Document("_id", 1))
                assertThat(statsIndexes[1]).containsEntry("key", Document(SUB_ACC_NO, 1))
            }

            suspend fun assertRequestFileIndexes() {
                val filesIndexes = mongoTemplate.collection<DocSubmissionRequestFile>().listIndexes().asFlow().toList()
                assertThat(filesIndexes).hasSize(8)

                assertThat(filesIndexes[0]).containsEntry("key", Document("_id", 1))
                assertThat(filesIndexes[1]).containsEntry("key", Document(RQT_FILE_SUB_ACC_NO, 1))
                assertThat(filesIndexes[2]).containsEntry("key", Document(RQT_FILE_SUB_VERSION, 1))
                assertThat(filesIndexes[3]).containsEntry("key", Document(RQT_FILE_PATH, 1))
                assertThat(filesIndexes[4]).containsEntry("key", Document(RQT_FILE_INDEX, 1))

                assertThat(filesIndexes[5]).containsEntry(
                    "key",
                    Document(RQT_FILE_SUB_ACC_NO, 1)
                        .append(RQT_FILE_SUB_VERSION, 1)
                        .append(RQT_FILE_PATH, 1),
                )
                assertThat(filesIndexes[6]).containsEntry(
                    "key",
                    Document(RQT_FILE_SUB_ACC_NO, 1)
                        .append(RQT_FILE_SUB_VERSION, 1)
                        .append(RQT_FILE_INDEX, 1),
                )
                assertThat(filesIndexes[7]).containsEntry(
                    "key",
                    Document(RQT_FILE_SUB_ACC_NO, 1)
                        .append(RQT_FILE_SUB_VERSION, 1)
                        .append(RQT_FILE_STATUS, 1),
                )
            }

            mongoTemplate.executeMigrations()

            assertSubmissionIndexes()
            assertRequestIndexes()
            assertFileListIndexes()
            assertStatsIndexes()
        }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
            register.add("app.mongo.execute-migrations") { "false" }
            register.add("app.mongo.migration-package") { "ac.uk.ebi.biostd.persistence.doc.migrations" }
        }
    }
}
