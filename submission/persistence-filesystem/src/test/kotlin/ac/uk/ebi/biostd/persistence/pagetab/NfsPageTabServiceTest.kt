package ac.uk.ebi.biostd.persistence.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.NfsPageTabService
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class NfsPageTabServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val serializationService: SerializationService
) {
    private val rootPath = tempFolder.root
    private val folderResolver = SubmissionFolderResolver(Paths.get("$rootPath/submission"), Paths.get("$rootPath/ftp"))
    private val testInstance = NfsPageTabService(folderResolver, serializationService)

    @Test
    fun `generate page tab`() {
        val fileList = ExtFileList("data/file-list", listOf())
        val submission = basicExtSubmission.copy(section = ExtSection(type = "Study", fileList = fileList))
        val subFolder = rootPath.resolve("submission/${submission.relPath}")

        setUpSerializer(fileList.toFilesTable())
        setUpSerializer(submission.toSimpleSubmission())

        assertThat(testInstance.generatePageTab(submission))
            .isEqualTo(submission.copy(pageTabFiles = pageTabFiles(subFolder)))

        verifyFileLists(subFolder)
        verifySubmissionFiles(subFolder)
    }

    private fun setUpSerializer(element: Any) {
        every { serializationService.serializeElement(element, JSON_PRETTY) } returns "json"
        every { serializationService.serializeElement(element, XML) } returns "xml"
        every { serializationService.serializeElement(element, TSV) } returns "tsv"
    }

    private fun pageTabFiles(submissionFolder: File) = listOf(
        NfsFile(SUB_JSON, submissionFolder.resolve(SUB_JSON)),
        NfsFile(SUB_XML, submissionFolder.resolve(SUB_XML)),
        NfsFile(SUB_TSV, submissionFolder.resolve(SUB_TSV))
    )

    private fun verifyFileLists(submissionFolder: File) {
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_JSON}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_XML}"))
        assertPageTabFile(submissionFolder.resolve("data/${FILE_LIST_TSV}"))
    }

    private fun verifySubmissionFiles(subFolder: File) {
        assertPageTabFile(subFolder.resolve(SUB_JSON))
        assertPageTabFile(subFolder.resolve(SUB_XML))
        assertPageTabFile(subFolder.resolve(SUB_TSV))
    }

    private fun assertPageTabFile(file: File) {
        assertThat(file).exists()
        assertThat(Files.getPosixFilePermissions(file.toPath())).containsExactlyInAnyOrderElementsOf(RW_R_____)
    }
}
