package ac.uk.ebi.biostd.persistence.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.FirePageTabService
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.FireFile
import uk.ac.ebi.fire.client.model.FireFile as FireWebFile
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireWebClient

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class FirePageTabServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val serializationService: SerializationService,
    @MockK private val fireWebClient: FireWebClient
) {
    private val fireFolder = tempFolder.root.resolve("fire-temp")
    private val testInstance: FirePageTabService = FirePageTabService(fireFolder, serializationService, fireWebClient)

    @Test
    fun `generate page tab`() {
        val fileList = ExtFileList("data/file-list", listOf())
        val submission = basicExtSubmission.copy(section = ExtSection(type = "Study", fileList = fileList))

        setUpSerializer(fileList.toFilesTable())
        setUpSerializer(submission.toSimpleSubmission())
        setUpFireWebClient()

        assertThat(testInstance.generatePageTab(submission)).isEqualTo(submission.copy(pageTabFiles = pageTabFiles()))
        verifyFileLists(fireFolder)
        verifySubmissionFiles(fireFolder)
    }

    private fun setUpSerializer(element: Any) {
        every { serializationService.serializeElement(element, SubFormat.JSON_PRETTY) } returns "json"
        every { serializationService.serializeElement(element, SubFormat.XML) } returns "xml"
        every { serializationService.serializeElement(element, SubFormat.TSV) } returns "tsv"
    }

    private fun setUpFireWebClient() {
        every { fireWebClient.save(any(), any(), fireFolder.absolutePath) } returns
                FireWebFile(1, "$SUB_JSON-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(2, "$SUB_XML-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(3, "$SUB_TSV-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(4, "$FILE_LIST_JSON-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(5, "$FILE_LIST_XML-fireId", "md5", 1, "creationTime") andThen
                FireWebFile(6, "$FILE_LIST_TSV-fireId", "md5", 1, "creationTime")
    }

    private fun pageTabFiles() = listOf(
        FireFile(SUB_JSON, "$SUB_JSON-fireId", "md5", 1, listOf()),
        FireFile(SUB_XML, "$SUB_XML-fireId", "md5", 1, listOf()),
        FireFile(SUB_TSV, "$SUB_TSV-fireId", "md5", 1, listOf())
    )
}
