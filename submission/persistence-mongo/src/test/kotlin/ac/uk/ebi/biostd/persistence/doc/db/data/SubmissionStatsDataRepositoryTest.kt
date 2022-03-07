package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.doc.db.repositories.getByAccNo
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ac.uk.ebi.biostd.persistence.doc.test.doc.STAT_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
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
    fun beforeEach() {
        testInstance.deleteAll()
    }

    @Test
    fun `update stat`() {
        val accNo = "S-BSST1"
        testInstance.save(testDocSubmission.copy(accNo = accNo))
        testInstance.updateStat(accNo, 1, SingleSubmissionStat(accNo, 4L, VIEWS))

        val stats = testInstance.getByAccNo(accNo).stats
        assertThat(stats).hasSize(1)
        assertThat(stats.first().value).isEqualTo(4L)
    }

    @Test
    fun `increment stat`() {
        val accNo = "S-BSST2"
        val increments = listOf(SingleSubmissionStat(accNo, 4L, VIEWS), SingleSubmissionStat(accNo, 8L, VIEWS))

        testInstance.save(testDocSubmission.copy(accNo = accNo))
        val increment = testInstance.incrementStat(accNo, 1, increments)

        assertThat(increment).isEqualTo(12L)

        val stats = testInstance.getByAccNo(accNo).stats
        assertThat(stats).hasSize(1)
        assertThat(stats.first().value).isEqualTo(STAT_VALUE + 4L + 8L)
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
