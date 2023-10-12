package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.StatNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ac.uk.ebi.biostd.persistence.doc.test.doc.STAT_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.SUB_ACC_NO
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
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
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds

@ExtendWith(MockKExtension::class, SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
@OptIn(ExperimentalCoroutinesApi::class)
class StatsMongoDataServiceTest(
    @MockK private val submissionsRepository: SubmissionMongoRepository,
    @Autowired private val submissionStatsDataRepository: SubmissionStatsDataRepository,
) {
    private val testInstance = StatsMongoDataService(submissionsRepository, submissionStatsDataRepository)

    @AfterEach
    fun afterEach() = runBlocking { submissionStatsDataRepository.deleteAll() }

    @Test
    fun `find all by type`() = runTest {
        val stats1 = DocSubmissionStats(ObjectId(), "S-TEST1", mapOf(VIEWS.value to 1L))
        val stats2 = DocSubmissionStats(ObjectId(), "S-TEST2", mapOf(VIEWS.value to 2L))

        submissionStatsDataRepository.save(stats1)
        submissionStatsDataRepository.save(stats2)

        val page1 = testInstance.findByType(VIEWS, PageRequest(limit = 1, offset = 0)).toList()
        assertThat(page1).hasSize(1)
        assertStat(page1.first(), "S-TEST1", 1L)

        val page2 = testInstance.findByType(VIEWS, PageRequest(limit = 1, offset = 1)).toList()
        assertThat(page2).hasSize(1)
        assertStat(page2.first(), "S-TEST2", 2L)
    }

    @Test
    fun `find by accNo and type`() = runTest {
        val testStat = DocSubmissionStats(ObjectId(), SUB_ACC_NO, mapOf(VIEWS.value to STAT_VALUE))
        submissionStatsDataRepository.save(testStat)
        assertStat(testInstance.findByAccNoAndType(SUB_ACC_NO, VIEWS), SUB_ACC_NO, STAT_VALUE)
    }

    @Test
    fun `find stats for non existing submission`() = runTest {
        val exception = assertThrows<StatNotFoundException> { testInstance.findByAccNoAndType("S-TEST1", VIEWS) }
        assertThat(exception.message)
            .isEqualTo("There is no submission stat registered with AccNo S-TEST1 and type VIEWS")
    }

    @Test
    fun `save single stat`() = runTest {
        coEvery { submissionsRepository.existsByAccNo("S-TEST1") } returns true

        val stat = SingleSubmissionStat("S-TEST1", 1L, VIEWS)
        val result = testInstance.save(stat)

        assertStat(result, "S-TEST1", 1L)
    }

    @Test
    fun `save single stat for non existing submission`() = runTest {
        coEvery { submissionsRepository.existsByAccNo("S-TEST1") } returns false

        val stat = SingleSubmissionStat("S-TEST1", 1L, VIEWS)
        val exception = assertThrows<SubmissionNotFoundException> { testInstance.save(stat) }
        assertThat(exception.message).isEqualTo("The submission 'S-TEST1' was not found")
    }

    @Test
    fun `save all stats`() = runTest {
        coEvery { submissionsRepository.existsByAccNo("S-TEST1") } returns true
        coEvery { submissionsRepository.existsByAccNo("S-TEST2") } returns true
        coEvery { submissionsRepository.existsByAccNo("S-TEST3") } returns false

        val stats = listOf(
            SingleSubmissionStat("S-TEST1", 1L, VIEWS),
            SingleSubmissionStat("S-TEST2", 2L, VIEWS),
            SingleSubmissionStat("S-TEST1", 3L, VIEWS),
            SingleSubmissionStat("S-TEST3", 4L, VIEWS)
        )

        val result = testInstance.saveAll(stats)
        assertThat(result).hasSize(2)
        assertThat(result.first()).isEqualToComparingFieldByField(stats.third())
        assertThat(result.second()).isEqualToComparingFieldByField(stats.second())
    }

    @Test
    fun `increment stats`() = runTest {
        coEvery { submissionsRepository.existsByAccNo("S-TEST1") } returns true
        coEvery { submissionsRepository.existsByAccNo("S-TEST2") } returns true
        coEvery { submissionsRepository.existsByAccNo("S-TEST3") } returns false

        val stats = listOf(
            SingleSubmissionStat("S-TEST1", 1L, VIEWS),
            SingleSubmissionStat("S-TEST2", 2L, VIEWS),
            SingleSubmissionStat("S-TEST1", 3L, VIEWS),
            SingleSubmissionStat("S-TEST3", 4L, VIEWS)
        )

        val existing = DocSubmissionStats(ObjectId(), "S-TEST2", mapOf(VIEWS.value to 3L))
        submissionStatsDataRepository.save(existing)

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
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
