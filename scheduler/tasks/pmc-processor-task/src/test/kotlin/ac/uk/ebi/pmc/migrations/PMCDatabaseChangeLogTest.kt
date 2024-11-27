package ac.uk.ebi.pmc.migrations

import ac.uk.ebi.pmc.config.ERRORS_COL
import ac.uk.ebi.pmc.config.INPUT_FILES_COL
import ac.uk.ebi.pmc.config.PersistenceConfig
import ac.uk.ebi.pmc.config.PropConfig
import ac.uk.ebi.pmc.config.SUBMISSION_COL
import ac.uk.ebi.pmc.config.SUB_FILES_COL
import ac.uk.ebi.pmc.persistence.docs.SubFileDocument.Fields.FILE_DOC_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.SubFileDocument.Fields.FILE_DOC_PATH
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_POS_IN_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_SOURCE_TIME
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_STATUS
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument.Fields.ERROR_ACCNO
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument.Fields.ERROR_MODE
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument.Fields.ERROR_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument.Fields.ERROR_UPLOADED
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [PropConfig::class, PersistenceConfig::class])
@Testcontainers
internal class PMCDatabaseChangeLogTest(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate,
) {
    @BeforeEach
    fun init(): Unit =
        runBlocking {
            mongoTemplate.collectionNames.asFlow()
                .collect { name -> mongoTemplate.dropCollection(name).awaitFirstOrNull() }
        }

    @Test
    fun `create schema migration 001 when collections does not exists`() =
        runTest {
            mongoTemplate.executeMigrations()

            assertErrorsCollection()
            assertSubmissionCollection()
            assertFilesSubmissionsCollection()
            assertInputFilesCollection()
        }

    @Test
    fun `create schema migration 001 when collections exists`() =
        runTest {
            mongoTemplate.createCollection(ERRORS_COL)
            mongoTemplate.createCollection(SUBMISSION_COL)
            mongoTemplate.createCollection(SUB_FILES_COL)
            mongoTemplate.createCollection(INPUT_FILES_COL)

            mongoTemplate.executeMigrations()

            assertErrorsCollection()
            assertSubmissionCollection()
            assertFilesSubmissionsCollection()
            assertInputFilesCollection()
        }

    private suspend fun ReactiveMongoTemplate.colIndexes(name: String): List<Document> {
        return mongoTemplate.getCollection(name)
            .awaitFirst().listIndexes().asFlow().toList()
            .map { it["key"] as Document }
    }

    private suspend fun assertErrorsCollection() {
        val indexes = mongoTemplate.colIndexes(ERRORS_COL)

        assertThat(mongoTemplate.collectionExists(ERRORS_COL).awaitFirst()).isTrue()
        assertThat(indexes).hasSize(5)

        assertThat(indexes).contains(Index().on("_id", ASC).indexKeys)
        assertThat(indexes).contains(Index().on(ERROR_SOURCE_FILE, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(ERROR_MODE, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(ERROR_ACCNO, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(ERROR_UPLOADED, ASC).indexKeys)
    }

    private suspend fun assertSubmissionCollection() {
        val indexes = mongoTemplate.colIndexes(SUBMISSION_COL)

        assertThat(mongoTemplate.collectionExists(SUBMISSION_COL).awaitFirst()).isTrue()
        assertThat(indexes).hasSize(8)

        assertThat(indexes).contains(Index().on("_id", ASC).indexKeys)
        assertThat(indexes).contains(Index().on(SUB_ACC_NO, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(SUB_ACC_NO, ASC).on(SUB_SOURCE_TIME, ASC).indexKeys)
        assertThat(indexes).contains(
            Index().on(SUB_ACC_NO, ASC).on(SUB_SOURCE_TIME, ASC).on(SUB_POS_IN_FILE, ASC).indexKeys,
        )
        assertThat(indexes).contains(Index().on(SUB_SOURCE_TIME, ASC).on(SUB_POS_IN_FILE, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(SUB_STATUS, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(SUB_SOURCE_FILE, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(SUB_SOURCE_TIME, ASC).indexKeys)
    }

    private suspend fun assertFilesSubmissionsCollection() {
        val indexes = mongoTemplate.colIndexes(SUB_FILES_COL)

        assertThat(mongoTemplate.collectionExists(SUB_FILES_COL).awaitFirst()).isTrue()
        assertThat(indexes).hasSize(1)

        assertThat(indexes).contains(Index().on("_id", ASC).indexKeys)
    }

    private suspend fun assertInputFilesCollection() {
        val indexes = mongoTemplate.colIndexes(INPUT_FILES_COL)

        assertThat(mongoTemplate.collectionExists(INPUT_FILES_COL).awaitFirst()).isTrue()
        assertThat(indexes).hasSize(3)

        assertThat(indexes).contains(Index().on("_id", ASC).indexKeys)
        assertThat(indexes).contains(Index().on(FILE_DOC_ACC_NO, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(FILE_DOC_PATH, ASC).indexKeys)
    }

    companion object {
        private const val DATABASE_TEST_NAME = "biostudies-test"

        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("app.data.mongodbUri") { mongoContainer.getReplicaSetUrl(DATABASE_TEST_NAME) }
            register.add("app.data.mongodbDatabase") { DATABASE_TEST_NAME }
            register.add("app.data.execute-migrations") { "false" }
        }
    }
}
