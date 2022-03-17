package uk.ac.ebi.scheduler.exporter.service

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.extensions.getExtSubmissionsAsSequence
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import com.fasterxml.jackson.core.JsonEncoding.UTF8
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.isCollection
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import mu.KotlinLogging
import uk.ac.ebi.scheduler.exporter.config.ApplicationProperties

private val logger = KotlinLogging.logger {}
internal const val SUBMISSIONS = "submissions"

class PublicOnlyExporterService(
    private val bioWebClient: BioWebClient,
    private val appProperties: ApplicationProperties,
    private val serializationService: SerializationService,
    private val toSubmissionMapper: ToSubmissionMapper
) {
    private lateinit var jsonWriter: JsonGenerator

    fun exportPublicSubmissions() {
        startExportFile()
        writeSubmissions()
        closeExportFile()
    }

    private fun startExportFile() {
        logger.info { "Exporting public submissions" }
        jsonWriter = jsonWriter()
        jsonWriter.writeStartObject()
        jsonWriter.writeArrayFieldStart(SUBMISSIONS)
    }

    private fun writeSubmissions() {
        bioWebClient
            .getExtSubmissionsAsSequence(ExtPageQuery(released = true, limit = 1))
            .forEach(::writeSubmission)
    }

    private fun writeSubmission(extSubmission: ExtSubmission) {
        if (extSubmission.isCollection.not()) {
            logger.info { "Exporting public submission '${extSubmission.accNo}'" }
            jsonWriter.writeRawValue(
                serializationService.serializeSubmission(
                    toSubmissionMapper.toSimpleSubmission(extSubmission),
                    SubFormat.JSON_PRETTY
                )
            )
        }
    }

    private fun closeExportFile() {
        val fileName = appProperties.fileName
        val output = Paths.get(appProperties.outputPath)

        jsonWriter.writeEndArray()
        jsonWriter.close()
        Files.move(output.resolve("${fileName}_temp.json"), output.resolve("$fileName.json"), REPLACE_EXISTING)
    }

    private fun jsonWriter(): JsonGenerator {
        val outputPath = Paths.get("${appProperties.outputPath}/${appProperties.fileName}_temp.json")
        Files.deleteIfExists(outputPath)

        return JsonFactory().createGenerator(outputPath.toFile(), UTF8).apply {
            prettyPrinter = DefaultPrettyPrinter()
        }
    }
}
