package ac.uk.ebi.biostd.stats.web

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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.multipart.MultipartFile
import uk.ac.ebi.stats.model.SubmissionStat
import uk.ac.ebi.stats.model.SubmissionStatType
import uk.ac.ebi.stats.model.SubmissionStatType.VIEWS
import uk.ac.ebi.stats.service.SubmissionStatsService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class StatsResourceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val statsFileHandler: StatsFileHandler,
    @MockK private val tempFileGenerator: TempFileGenerator,
    @MockK private val statsService: SubmissionStatsService
) {
    private val testStat = SubmissionStat("S-TEST123", 10, VIEWS)
    private val testInstance = StatsResource(statsFileHandler, tempFileGenerator, statsService)
    private val mvc = MockMvcBuilders.standaloneSetup(testInstance).build()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `find by type`() {
        val type = slot<SubmissionStatType>()
        val expectedResponse = jsonArray(jsonObj {
            "accNo" to "S-TEST123"
            "type" to "VIEWS"
            "value" to 10
        })

        every { statsService.findByType(capture(type)) } returns listOf(testStat)

        mvc.get("/stats/views") {
            accept = APPLICATION_JSON
        }.andExpect {
            status { isOk }
            content { json(expectedResponse.toString()) }
        }

        assertThat(type.captured).isEqualTo(VIEWS)
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
    fun register() {
        val savedStat = slot<SubmissionStat>()
        val body = jsonObj {
            "accNo" to "S-TEST123"
            "type" to "VIEWS"
            "value" to 10
        }.toString()

        every { statsService.save(capture(savedStat)) } returns testStat

        mvc.post("/stats") {
            accept = APPLICATION_JSON
            contentType = APPLICATION_JSON
            content = body
        }.andExpect {
            status { isOk }
            content { json(body) }
        }

        verify(exactly = 1) { statsService.save(savedStat.captured) }
    }

    @Test
    fun `register from file`() {
        val savedStats = slot<List<SubmissionStat>>()
        val type = slot<SubmissionStatType>()
        val multipartStatsFile = slot<MultipartFile>()
        val statsFile = tempFolder.createFile("stats.tsv")
        val body = jsonArray(jsonObj {
            "accNo" to "S-TEST123"
            "type" to "VIEWS"
            "value" to 10
        }).toString()

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
}
