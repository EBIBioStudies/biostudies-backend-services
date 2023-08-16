package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ac.uk.ebi.biostd.stats.domain.service.SubmissionStatsService
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.multipart.MultipartFile

@ExtendWith(MockKExtension::class)
class StatsResourceTest(
    @MockK private val submissionStatsService: SubmissionStatsService,
) {
    private val testInstance = StatsResource(submissionStatsService)
    private val mvc = MockMvcBuilders.standaloneSetup(testInstance).build()

    private val testStat = SingleSubmissionStat("S-TEST123", 10, VIEWS)
    private val testJsonStat = jsonObj {
        "accNo" to "S-TEST123"
        "type" to "VIEWS"
        "value" to 10
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `find by accNo`() = runTest {
        val expectedResponse = jsonArray(testJsonStat)

        coEvery { submissionStatsService.findByAccNo("S-TEST123") } returns listOf(testStat)

        mvc.get("/stats/submission/S-TEST123") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(expectedResponse.toString()) }
        }
    }

    @Test
    fun `find by type`() = runTest {
        val filter = slot<PaginationFilter>()
        val expectedResponse = jsonArray(testJsonStat)

        every { submissionStatsService.findByType("views", capture(filter)) } returns flowOf(testStat)

        mvc.get("/stats/views") {
            param("limit", "1")
            param("offset", "1")
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(expectedResponse.toString()) }
        }

        assertThat(filter.captured.limit).isEqualTo(1)
        assertThat(filter.captured.offset).isEqualTo(1)
    }

    @Test
    fun `find by type and accNo`() = runTest {
        coEvery { submissionStatsService.findByAccNoAndType("S-TEST123", "views") } returns testStat

        mvc.get("/stats/views/S-TEST123") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { json(testJsonStat.toString()) }
        }
    }

    @Test
    fun `register stat`() = runTest {
        val statSlot = slot<SubmissionStat>()

        coEvery { submissionStatsService.register(capture(statSlot)) } returns testStat

        mvc.post("/stats") {
            accept = APPLICATION_JSON
            contentType = APPLICATION_JSON
            content = testJsonStat
        }.andExpect {
            status { isOk() }
            content { json(testJsonStat.toString()) }
        }

        coVerify(exactly = 1) { submissionStatsService.register(statSlot.captured) }
    }

    @Test
    fun `register from file`() = runTest {
        val multipartStatsFile = slot<MultipartFile>()
        val body = jsonArray(testJsonStat).toString()

        coEvery { submissionStatsService.register("views", capture(multipartStatsFile)) } returns listOf(testStat)

        mvc.multipart("/stats/views") {
            accept = APPLICATION_JSON
            file("stats", body.toByteArray())
        }.andExpect {
            status { isOk() }
            content { json(body) }
        }

        coVerify(exactly = 1) { submissionStatsService.register("views", multipartStatsFile.captured) }
    }

    @Test
    fun `increment from file`() = runTest {
        val multipartStatsFile = slot<MultipartFile>()
        val body = jsonArray(testJsonStat).toString()

        coEvery { submissionStatsService.increment("views", capture(multipartStatsFile)) } returns listOf(testStat)

        mvc.multipart("/stats/views/increment") {
            accept = APPLICATION_JSON
            file("stats", body.toByteArray())
        }.andExpect {
            status { isOk() }
            content { json(body) }
        }

        coVerify(exactly = 1) {
            submissionStatsService.increment("views", multipartStatsFile.captured)
        }
    }
}
