package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ac.uk.ebi.biostd.persistence.doc.test.doc.STAT_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
class StatsMongoDataServiceTest(
    @Autowired private val submissionStatsDataRepository: SubmissionStatsDataRepository
) {
    private val testInstance = StatsMongoDataService(submissionStatsDataRepository)

    @AfterEach
    fun afterEach() = submissionStatsDataRepository.deleteAll()

    @Test
    fun `find all by type`() {
        val submission1 = testDocSubmission.copy(accNo = "S-TEST1", stats = listOf(DocStat(VIEWS.name, 1L)))
        val submission2 = testDocSubmission.copy(accNo = "S-TEST2", stats = listOf(DocStat(VIEWS.name, 2L)))

        submissionStatsDataRepository.save(submission1)
        submissionStatsDataRepository.save(submission2)

        val page1 = testInstance.findByType(VIEWS, PaginationFilter(limit = 1, offset = 0))
        assertThat(page1).hasSize(1)
        assertStat(page1.first(), "S-TEST1", 1L)

        val page2 = testInstance.findByType(VIEWS, PaginationFilter(limit = 1, offset = 1))
        assertThat(page2).hasSize(1)
        assertStat(page2.first(), "S-TEST2", 2L)
    }

    @Test
    fun `find by accNo and type`() {
        submissionStatsDataRepository.save(testDocSubmission)
        assertStat(testInstance.findByAccNoAndType(SUB_ACC_NO, VIEWS), SUB_ACC_NO, STAT_VALUE)
    }

    @Test
    fun `find stats for non existing submission`() {
        val exception = assertThrows<StatNotFoundException> { testInstance.findByAccNoAndType("S-TEST1", VIEWS) }
        assertThat(exception.message)
            .isEqualTo("There is no submission stat registered with AccNo S-TEST1 and type VIEWS")
    }

    @Test
    fun `save all stats`() {
        val stats = listOf(
            SingleSubmissionStat("S-TEST1", 1L, VIEWS),
            SingleSubmissionStat("S-TEST2", 2L, VIEWS),
            SingleSubmissionStat("S-TEST1", 3L, VIEWS),
            SingleSubmissionStat("S-TEST3", 4L, VIEWS)
        )

        submissionStatsDataRepository.save(testDocSubmission.copy(accNo = "S-TEST1", stats = listOf()))
        submissionStatsDataRepository.save(testDocSubmission.copy(accNo = "S-TEST2", stats = listOf()))

        val result = testInstance.saveAll(stats)
        assertThat(result).hasSize(2)
        assertThat(result.first()).isEqualToComparingFieldByField(stats.third())
        assertThat(result.second()).isEqualToComparingFieldByField(stats.second())
    }

    @Test
    fun `increment stats`() {
        val stats = listOf(
            SingleSubmissionStat("S-TEST1", 1L, VIEWS),
            SingleSubmissionStat("S-TEST2", 2L, VIEWS),
            SingleSubmissionStat("S-TEST1", 3L, VIEWS),
            SingleSubmissionStat("S-TEST3", 4L, VIEWS)
        )

        submissionStatsDataRepository.save(testDocSubmission.copy(accNo = "S-TEST1", stats = listOf()))
        submissionStatsDataRepository.save(
            testDocSubmission.copy(accNo = "S-TEST2", stats = listOf(DocStat(VIEWS.name, 3L)))
        )

        val result = testInstance.incrementAll(stats)
        assertThat(result).hasSize(2)
        assertStat(result.first(), "S-TEST1", 4L)
        assertStat(result.second(), "S-TEST2", 5L)
    }

    private fun assertStat(stat: SubmissionStat, accNo: String, value: Long) {
        assertThat(stat.accNo).isEqualTo(accNo)
        assertThat(stat.type).isEqualTo(VIEWS)
        assertThat(stat.value).isEqualTo(value)
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
            register.add("app.persistence.enableMongo") { "true" }
        }
    }
}
