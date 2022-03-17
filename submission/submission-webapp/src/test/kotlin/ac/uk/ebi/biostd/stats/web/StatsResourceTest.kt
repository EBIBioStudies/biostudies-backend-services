package ac.uk.ebi.biostd.stats.web

import ac.uk.ebi.biostd.persistence.common.model.SubmissionStat
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType
import ac.uk.ebi.biostd.persistence.common.model.SubmissionStatType.VIEWS
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.StatsDataService
import ac.uk.ebi.biostd.persistence.doc.model.SingleSubmissionStat
import ac.uk.ebi.biostd.stats.web.handlers.StatsFileHandler
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.multipart.MultipartFile

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class StatsResourceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val statsFileHandler: StatsFileHandler,
    @MockK private val tempFileGenerator: TempFileGenerator,
    @MockK private val statsService: StatsDataService
) {
    private val testStat = SingleSubmissionStat("S-TEST123", 10, VIEWS)
    private val testInstance = StatsResource(statsFileHandler, tempFileGenerator, statsService)
    private val mvc = MockMvcBuilders.standaloneSetup(testInstance).build()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `find by type`() {
        val type = slot<SubmissionStatType>()
        val filter = slot<PaginationFilter>()
        val expectedResponse = jsonArray(
            jsonObj {
                "accNo" to "S-TEST123"
                "type" to "VIEWS"
                "value" to 10
            }
        )

        every { statsService.findByType(capture(type), capture(filter)) } returns listOf(testStat)

        mvc.get("/stats/views") {
            param("limit", "1")
            param("offset", "1")
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk }
            content { json(expectedResponse.toString()) }
        }

        assertThat(type.captured).isEqualTo(VIEWS)
        assertThat(filter.captured.limit).isEqualTo(1)
        assertThat(filter.captured.offset).isEqualTo(1)
    }

    @Test
    fun `find by type and accNo`() {
        val type = slot<SubmissionStatType>()
        val expectedResponse = jsonObj {
            "accNo" to "S-TEST123"
            "type" to "VIEWS"
            "value" to 10
        }

        every { statsService.findByAccNoAndType("S-TEST123", capture(type)) } returns testStat

        mvc.get("/stats/views/S-TEST123") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk }
            content { json(expectedResponse.toString()) }
        }

        assertThat(type.captured).isEqualTo(VIEWS)
    }

    @Test
    fun `register from file`() {
        val savedStats = slot<List<SubmissionStat>>()
        val type = slot<SubmissionStatType>()
        val multipartStatsFile = slot<MultipartFile>()
        val statsFile = tempFolder.createFile("stats.tsv")
        val body = jsonArray(
            jsonObj {
                "accNo" to "S-TEST123"
                "type" to "VIEWS"
                "value" to 10
            }
        ).toString()

        every { statsService.saveAll(capture(savedStats)) } returns listOf(testStat)
        every { tempFileGenerator.asFile(capture(multipartStatsFile)) } returns statsFile
        every { statsFileHandler.readStats(statsFile, capture(type)) } returns listOf(testStat)

        mvc.multipart("/stats/views") {
            accept = APPLICATION_JSON
            file("stats", body.toByteArray())
        }.andExpect {
            status { isOk }
            content { json(body) }
        }

        assertThat(type.captured).isEqualTo(VIEWS)
        verify(exactly = 1) { statsService.saveAll(savedStats.captured) }
        verify(exactly = 1) { tempFileGenerator.asFile(multipartStatsFile.captured) }
        verify(exactly = 1) { statsFileHandler.readStats(statsFile, type.captured) }
    }

    @Test
    fun `increment from file`() {
        val incrementedStats = slot<List<SubmissionStat>>()
        val type = slot<SubmissionStatType>()
        val multipartStatsFile = slot<MultipartFile>()
        val incrementedStat = SingleSubmissionStat("S-TEST123", 20, VIEWS)
        val statsFile = tempFolder.createFile("increase.tsv")
        val body = jsonArray(
            jsonObj {
                "accNo" to "S-TEST123"
                "type" to "VIEWS"
                "value" to 10
            }
        ).toString()
        val response = jsonArray(
            jsonObj {
                "accNo" to "S-TEST123"
                "type" to "VIEWS"
                "value" to 20
            }
        ).toString()

        every { tempFileGenerator.asFile(capture(multipartStatsFile)) } returns statsFile
        every { statsFileHandler.readStats(statsFile, capture(type)) } returns listOf(testStat)
        every { statsService.incrementAll(capture(incrementedStats)) } returns listOf(incrementedStat)

        mvc.multipart("/stats/views/increment") {
            accept = APPLICATION_JSON
            file("stats", body.toByteArray())
        }.andExpect {
            status { isOk }
            content { json(response) }
        }

        assertThat(type.captured).isEqualTo(VIEWS)
        verify(exactly = 1) { statsService.incrementAll(incrementedStats.captured) }
        verify(exactly = 1) { tempFileGenerator.asFile(multipartStatsFile.captured) }
        verify(exactly = 1) { statsFileHandler.readStats(statsFile, type.captured) }
    }
}
