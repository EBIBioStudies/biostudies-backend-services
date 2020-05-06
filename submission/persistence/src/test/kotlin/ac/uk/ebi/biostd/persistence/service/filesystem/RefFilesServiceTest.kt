package ac.uk.ebi.biostd.persistence.service.filesystem

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.integration.FileMode
import ac.uk.ebi.biostd.persistence.test.extSubmissionWithFileList
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class RefFilesServiceTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockSerializationService: SerializationService
) {
    private val sectionFile = temporaryFolder.createFile("file.txt", "text-1")
    private val sectionFolder = temporaryFolder.createDirectory("fileDirectory")
    private var sectionFolderFile = sectionFolder.createNewFile("file3.txt", "folder-file-content")
    private val referencedFile = temporaryFolder.createFile("file2.txt", "text-3")
    private val extSubmission = extSubmissionWithFileList(listOf(sectionFile, sectionFolder), listOf(referencedFile))

    private val testInstance =
        RefFilesService(SubmissionFolderResolver(temporaryFolder.root.toPath()), mockSerializationService)

    @BeforeEach
    fun beforeEach() {
        val simpleSubmission = extSubmission.toSimpleSubmission()
        every { mockSerializationService.serializeElement(simpleSubmission, XML) } returns ""
        every { mockSerializationService.serializeElement(simpleSubmission, TSV) } returns ""
        every { mockSerializationService.serializeElement(simpleSubmission, JSON_PRETTY) } returns ""

        every { mockSerializationService.serializeElement(any<FilesTable>(), XML) } returns ""
        every { mockSerializationService.serializeElement(any<FilesTable>(), TSV) } returns ""
        every { mockSerializationService.serializeElement(any<FilesTable>(), JSON_PRETTY) } returns ""
    }

    @Test
    fun whenMove() {
        testPersistSubmissionFiles(FileMode.MOVE)
    }

    @Test
    fun whenCopy() {
        testPersistSubmissionFiles(FileMode.COPY)
    }

    private fun testPersistSubmissionFiles(mode: FileMode) {
        testInstance.persistSubmissionFiles(extSubmission, mode)

        val relPath = extSubmission.relPath

        assertThat(getPath("submission/$relPath/Files/file.txt")).exists()
        assertThat(getPath("submission/$relPath/Files/file2.txt")).exists()

        val directory = getPath("submission/$relPath/Files/fileDirectory").toFile()
        assertThat(FileUtils.listFiles(directory).first()).hasContent("folder-file-content")
        assertThat(FileUtils.listFiles(directory).first()).hasName("file3.txt")

        assertThat(getPath("submission/$relPath/ABC-123.xml")).exists()
        assertThat(getPath("submission/$relPath/ABC-123.json")).exists()
        assertThat(getPath("submission/$relPath/ABC-123.pagetab.tsv")).exists()

        assertThat(getPath("submission/$relPath/fileList.xml")).exists()
        assertThat(getPath("submission/$relPath/fileList.json")).exists()
        assertThat(getPath("submission/$relPath/fileList.pagetab.tsv")).exists()
    }

    private fun getPath(path: String) = Paths.get("${temporaryFolder.root.absolutePath}/$path")
}
