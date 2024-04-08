package uk.ac.ebi.scheduler.stats.persistence

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod.PAGE_TAB
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.extended.model.StorageMode.FIRE
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import uk.ac.ebi.scheduler.stats.config.PersistenceConfig
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository.Companion.IMAGING_COLLECTION
import uk.ac.ebi.scheduler.stats.persistence.StatsReporterDataRepository.Companion.STATS_COLLECTION_KEY
import java.time.Duration.ofSeconds
import java.time.Instant

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [PersistenceConfig::class])
class StatsReporterDataRepositoryTest(
    @Autowired private val mongoTemplate: ReactiveMongoTemplate,
    @Autowired private val testInstance: StatsReporterDataRepository,
) {
    @BeforeEach
    fun beforeEach() {
        setUpSubmissions()
        setUpStats()
    }

    @Test
    fun calculateStats() =
        runTest {
            val imagingStats = testInstance.calculateImagingFilesSize()
            val nonImagingStats = testInstance.calculateNonImagingFilesSize()

            assertThat(imagingStats).isEqualTo(10L)
            assertThat(nonImagingStats).isEqualTo(19L)
        }

    private fun setUpSubmissions() {
        val arrayExpress = listOf(DocCollection(AE_COLLECTION))
        val bioImages = listOf(DocCollection(IMAGING_COLLECTION))

        fun save(submission: DocSubmission) = mongoTemplate.save(submission, SUBMISSIONS_COLLECTION_KEY).block()

        save(testSub)
        save(testSub.copy(id = ObjectId(), accNo = "S-BSST2", version = -1))
        save(testSub.copy(id = ObjectId(), accNo = "S-BSST3"))
        save(testSub.copy(id = ObjectId(), accNo = "S-BIAD1", collections = bioImages))
        save(testSub.copy(id = ObjectId(), accNo = "S-BIAD2", version = -1, collections = bioImages))
        save(testSub.copy(id = ObjectId(), accNo = "S-BIAD3", collections = bioImages))
        save(testSub.copy(id = ObjectId(), accNo = "E-MTAB1", collections = arrayExpress))
        save(testSub.copy(id = ObjectId(), accNo = "E-MTAB2", collections = arrayExpress))
        save(testSub.copy(id = ObjectId(), accNo = "E-MTAB3", version = -1, collections = arrayExpress))
    }

    private fun setUpStats() {
        fun save(stat: DocSubmissionStats) = mongoTemplate.save(stat, STATS_COLLECTION_KEY).block()

        save(DocSubmissionStats(ObjectId(), accNo = "S-BSST1", mapOf(FILES_SIZE.value to 1, VIEWS.value to 10)))
        save(DocSubmissionStats(ObjectId(), accNo = "S-BSST2", mapOf(FILES_SIZE.value to 2, VIEWS.value to 12)))
        save(DocSubmissionStats(ObjectId(), accNo = "S-BSST3", mapOf(FILES_SIZE.value to 3)))
        save(DocSubmissionStats(ObjectId(), accNo = "S-BIAD1", mapOf(FILES_SIZE.value to 4, VIEWS.value to 11)))
        save(DocSubmissionStats(ObjectId(), accNo = "S-BIAD2", mapOf(FILES_SIZE.value to 5)))
        save(DocSubmissionStats(ObjectId(), accNo = "S-BIAD3", mapOf(FILES_SIZE.value to 6)))
        save(DocSubmissionStats(ObjectId(), accNo = "E-MTAB1", mapOf(FILES_SIZE.value to 7, VIEWS.value to 14)))
        save(DocSubmissionStats(ObjectId(), accNo = "E-MTAB2", mapOf(FILES_SIZE.value to 8, VIEWS.value to 16)))
        save(DocSubmissionStats(ObjectId(), accNo = "E-MTAB3", mapOf(FILES_SIZE.value to 9, VIEWS.value to 16)))
    }

    companion object {
        private const val AE_COLLECTION = "ArrayExpress"
        private const val SUBMISSIONS_COLLECTION_KEY = "submissions"
        private val testSub =
            DocSubmission(
                id = ObjectId(),
                accNo = "S-BSST1",
                version = 1,
                schemaVersion = "1.0",
                owner = "biostudies-dev@ebi.ac.uk",
                submitter = "biostudies-dev@ebi.ac.uk",
                title = "Test Stats Submission",
                doi = "10.983/S-BSST1",
                method = PAGE_TAB,
                rootPath = null,
                relPath = "S-BSST/001/S-BSST1",
                released = true,
                secretKey = "",
                creationTime = Instant.now(),
                modificationTime = Instant.now(),
                releaseTime = Instant.now(),
                section = DocSection(id = ObjectId(), type = "Study"),
                storageMode = FIRE,
            )

        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
