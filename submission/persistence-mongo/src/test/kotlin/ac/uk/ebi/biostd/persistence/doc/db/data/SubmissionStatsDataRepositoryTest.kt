package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.FILES_SIZE
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
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

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class SubmissionStatsDataRepositoryTest {
    @Autowired
    lateinit var testInstance: SubmissionStatsDataRepository

    @BeforeEach
    fun beforeEach() = runTest {
        testInstance.deleteAll().awaitSingleOrNull()
    }

    @Test
    fun `update non existing stat`() = runTest {
        val accNo = "S-BSST2"
        testInstance.updateOrRegisterStat(SingleSubmissionStat(accNo, 4L, VIEWS))

        val stats = testInstance.getByAccNo(accNo).stats
        assertThat(stats).hasSize(1)
        assertThat(stats[VIEWS.value]).isEqualTo(4L)
    }

    @Test
    fun `update existing stat`() = runTest {
        val accNo = "S-BSST1"
        testInstance.saveStats(DocSubmissionStats(ObjectId(), accNo, mapOf(FILES_SIZE.value to 1L)))
        testInstance.updateOrRegisterStat(SingleSubmissionStat(accNo, 4L, VIEWS))

        val stats = testInstance.getByAccNo(accNo).stats
        assertThat(stats).hasSize(2)
        assertThat(stats[VIEWS.value]).isEqualTo(4L)
        assertThat(stats[FILES_SIZE.value]).isEqualTo(1L)
    }

    @Test
    fun `increment stat`() = runTest {
        val accNo = "S-BSST3"
        val increments = listOf(SingleSubmissionStat(accNo, 4L, VIEWS), SingleSubmissionStat(accNo, 8L, VIEWS))

        testInstance.saveStats(DocSubmissionStats(ObjectId(), accNo, mapOf(VIEWS.value to 1L)))
        testInstance.incrementStat(accNo, increments)

        val stats = testInstance.getByAccNo(accNo).stats
        assertThat(stats).hasSize(1)
        assertThat(stats[VIEWS.value]).isEqualTo(13L)
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
        }
    }
}
