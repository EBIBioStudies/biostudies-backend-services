package uk.ac.ebi.scheduler.exporter.service

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.extensions.getExtSubmissionsAsSequence
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.scheduler.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.exporter.config.BioStudies
import java.nio.file.Paths

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class ExporterServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val bioWebClient: BioWebClient,
    @MockK private val serializationService: SerializationService
) {
    private val testInstance = ExporterService(bioWebClient, testProperties(), serializationService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockBioWebClient()
        mockSerializationService()
    }

    @Test
    fun `export public submissions`() {
        testInstance.exportPublicSubmissions()

        val output = Paths.get("${tempFolder.root.path}/publicOnlyStudies.json").toFile()
        val expectedJson = jsonObj {
            "submissions" to jsonArray(
                jsonObj {
                    "accNo" to "S-TEST123"
                }
            )
        }

        assertThat(output.exists()).isTrue()
        assertThat(Paths.get("${tempFolder.root.path}/publicOnlyStudies_temp.json").toFile().exists()).isFalse()
        assertThat(output.readText()).isEqualToIgnoringWhitespace(expectedJson.toString())
    }

    private fun mockBioWebClient() {
        mockkStatic("ac.uk.ebi.biostd.client.extensions.BioWebClientExtKt")
        every {
            bioWebClient.getExtSubmissionsAsSequence(eq(ExtPageQuery(released = true, limit = 1)))
        } returns sequenceOf(basicExtSubmission)
    }

    private fun mockSerializationService() {
        val serializedSubmission = jsonObj { "accNo" to "S-TEST123" }

        every {
            serializationService.serializeSubmission(basicExtSubmission.toSimpleSubmission(), JSON_PRETTY)
        } returns serializedSubmission.toString()
    }

    private fun testProperties() = ApplicationProperties().apply {
        this.fileName = "publicOnlyStudies"
        this.outputPath = tempFolder.root.path

        this.bioStudies = BioStudies().apply {
            url = "http://localhost:8080"
            user = "admin_user@ebi.ac.uk"
            password = "123456"
        }
    }
}
