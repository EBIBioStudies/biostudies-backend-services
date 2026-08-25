package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.integration.LockConfig
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbServicesConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod.PAGE_TAB
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.service.StatsMongoDataService.Companion.IMAGING_COLLECTION
import ac.uk.ebi.biostd.persistence.doc.test.beans.TestConfig
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
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds
import java.time.Instant

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbServicesConfig::class, TestConfig::class, LockConfig::class])
class StatsMongoDataServiceTest(
    @param:Autowired private val testInstance: StatsDataService,
    @param:Autowired private val mongoTemplate: ReactiveMongoTemplate,
) {
    @BeforeEach
    fun beforeEach() {
        mongoTemplate.remove(Query(), SUBMISSIONS_COLLECTION_KEY).block()
        mongoTemplate.remove(Query(), DocSubmissionStats::class.java).block()
        setUpSubmissions()
        setUpStats()
    }

    @Test
    fun calculateStats() =
        runTest {
            val imagingStats = testInstance.calculateImagingStats()
            assertThat(imagingStats.all.count).isEqualTo(2)
            assertThat(imagingStats.all.filesSize).isEqualTo(10L)
            assertThat(imagingStats.public.count).isEqualTo(1)
            assertThat(imagingStats.public.filesSize).isEqualTo(4L)

            val nonImagingStats = testInstance.calculateNonImagingStats()
            assertThat(nonImagingStats.all.count).isEqualTo(3L)
            assertThat(nonImagingStats.all.filesSize).isEqualTo(19L)
            assertThat(nonImagingStats.public.count).isEqualTo(1)
            assertThat(nonImagingStats.public.filesSize).isEqualTo(7L)

            val aeStats = testInstance.calculateAEStats()
            assertThat(aeStats.all.count).isEqualTo(2)
            assertThat(aeStats.all.filesSize).isEqualTo(16L)
            assertThat(aeStats.public.count).isEqualTo(1)
            assertThat(aeStats.public.filesSize).isEqualTo(7L)
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
        val arrayExpress = listOf(AE_COLLECTION)
        val bioImages = listOf(IMAGING_COLLECTION)

        fun save(
            accNo: String,
            stats: Map<String, Long>,
            collections: List<String> = emptyList(),
            released: Boolean = testSub.released,
        ) {
            val stat =
                DocSubmissionStats(
                    id = ObjectId(),
                    accNo = accNo,
                    version = testSub.version,
                    released = released,
                    stats = stats,
                    subCreationTime = testSub.creationTime,
                    subReleaseTime = testSub.releaseTime,
                    subModificationTime = testSub.modificationTime,
                    storageMode = testSub.storageMode,
                    collections = collections,
                )
            mongoTemplate.save(stat).block()
        }

        save(accNo = "S-BSST3", stats = mapOf(FILES_SIZE.value to 3), released = false)
        save(accNo = "S-BIAD1", stats = mapOf(FILES_SIZE.value to 4, VIEWS.value to 11), collections = bioImages)
        save(accNo = "S-BIAD3", stats = mapOf(FILES_SIZE.value to 6), collections = bioImages, released = false)
        save(accNo = "E-MTAB1", stats = mapOf(FILES_SIZE.value to 7, VIEWS.value to 14), collections = arrayExpress)
        save(
            accNo = "E-MTAB2",
            stats = mapOf(FILES_SIZE.value to 9, VIEWS.value to 16),
            collections = arrayExpress,
            released = false,
        )
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
                submissionTime = Instant.now(),
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
