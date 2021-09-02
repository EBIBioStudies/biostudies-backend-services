package ac.uk.ebi.biostd.persistence.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.NfsPageTabService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
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
import java.nio.file.Files.getPosixFilePermissions
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

        testInstance.generatePageTab(submission)

        verifyFileLists(subFolder)
        verifySubmissionFiles(subFolder)
    }

    private fun verifySubmissionFiles(subFolder: File) {
        assertPageTabFile(subFolder.resolve("S-TEST123.xml"))
        assertPageTabFile(subFolder.resolve("S-TEST123.json"))
        assertPageTabFile(subFolder.resolve("S-TEST123.pagetab.tsv"))
    }

    private fun verifyFileLists(subFolder: File) {
        val submissionFiles = subFolder.resolve("Files")
        assertPageTabFile(submissionFiles.resolve("data/file-list.xml"))
        assertPageTabFile(submissionFiles.resolve("data/file-list.json"))
        assertPageTabFile(submissionFiles.resolve("data/file-list.pagetab.tsv"))
    }

    private fun assertPageTabFile(file: File) {
        assertThat(file).exists()
        assertThat(getPosixFilePermissions(file.toPath())).containsExactlyInAnyOrderElementsOf(RW_R_____)
    }

    private fun setUpSerializer(element: Any) {
        every { serializationService.serializeElement(element, XML) } returns "xml"
        every { serializationService.serializeElement(element, TSV) } returns "tsv"
        every { serializationService.serializeElement(element, JSON_PRETTY) } returns "json"
    }
}
