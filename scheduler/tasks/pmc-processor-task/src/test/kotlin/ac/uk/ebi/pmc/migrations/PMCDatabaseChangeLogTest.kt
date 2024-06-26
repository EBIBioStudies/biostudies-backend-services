package ac.uk.ebi.pmc.migrations

import ac.uk.ebi.pmc.config.ERRORS_COL
import ac.uk.ebi.pmc.config.INPUT_FILES_COL
import ac.uk.ebi.pmc.config.PersistenceConfig
import ac.uk.ebi.pmc.config.PersistenceConfig.Companion.createMongockConfig
import ac.uk.ebi.pmc.config.PropConfig
import ac.uk.ebi.pmc.config.SUBMISSION_COL
import ac.uk.ebi.pmc.config.SUB_FILES_COL
import ac.uk.ebi.pmc.persistence.docs.FileDoc.Fields.FILE_DOC_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.FileDoc.Fields.FILE_DOC_PATH
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_POS_IN_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_SOURCE_TIME
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_STATUS
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc.Fields.ERROR_ACCNO
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc.Fields.ERROR_MODE
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc.Fields.ERROR_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc.Fields.ERROR_UPLOADED
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
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
    @Autowired private val springContext: ApplicationContext,
    @Autowired private val mongoTemplate: MongoTemplate,
) {
    @BeforeEach
    fun init() {
        mongoTemplate.collectionNames.forEach { name -> mongoTemplate.dropCollection(name) }
    }

    @Test
    fun `create schema migration 001 when collections does not exists`() {
        runPMCMigrations()

        assertErrorsCollection()
        assertSubmissionCollection()
        assertFilesSubmissionsCollection()
        assertInputFilesCollection()
    }

    @Test
    fun `create schema migration 001 when collections exists`() {
        mongoTemplate.createCollection(ERRORS_COL)
        mongoTemplate.createCollection(SUBMISSION_COL)
        mongoTemplate.createCollection(SUB_FILES_COL)
        mongoTemplate.createCollection(INPUT_FILES_COL)

        runPMCMigrations()

        assertErrorsCollection()
        assertSubmissionCollection()
        assertFilesSubmissionsCollection()
        assertInputFilesCollection()
    }

    private fun assertErrorsCollection() {
        val indexes = mongoTemplate.getCollection(ERRORS_COL).listIndexes().map { it["key"]!! }

        assertThat(mongoTemplate.collectionExists(ERRORS_COL)).isTrue
        assertThat(indexes).hasSize(5)

        assertThat(indexes).contains(Index().on("_id", ASC).indexKeys)
        assertThat(indexes).contains(Index().on(ERROR_SOURCE_FILE, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(ERROR_MODE, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(ERROR_ACCNO, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(ERROR_UPLOADED, ASC).indexKeys)
    }

    private fun assertSubmissionCollection() {
        val indexes = mongoTemplate.getCollection(SUBMISSION_COL).listIndexes().map { it["key"]!! }

        assertThat(mongoTemplate.collectionExists(SUBMISSION_COL)).isTrue
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

    private fun assertFilesSubmissionsCollection() {
        val indexes = mongoTemplate.getCollection(SUB_FILES_COL).listIndexes().map { it["key"]!! }

        assertThat(mongoTemplate.collectionExists(SUB_FILES_COL)).isTrue
        assertThat(indexes).hasSize(1)

        assertThat(indexes).contains(Index().on("_id", ASC).indexKeys)
    }

    private fun assertInputFilesCollection() {
        val indexes = mongoTemplate.getCollection(INPUT_FILES_COL).listIndexes().map { it["key"]!! }

        assertThat(mongoTemplate.collectionExists(INPUT_FILES_COL)).isTrue
        assertThat(indexes).hasSize(3)

        assertThat(indexes).contains(Index().on("_id", ASC).indexKeys)
        assertThat(indexes).contains(Index().on(FILE_DOC_ACC_NO, ASC).indexKeys)
        assertThat(indexes).contains(Index().on(FILE_DOC_PATH, ASC).indexKeys)
    }

    private fun runPMCMigrations() {
        val runner =
            createMongockConfig(
                mongoTemplate,
                springContext,
                "ac.uk.ebi.pmc.migrations",
            )
        runner.run(DefaultApplicationArguments())
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
