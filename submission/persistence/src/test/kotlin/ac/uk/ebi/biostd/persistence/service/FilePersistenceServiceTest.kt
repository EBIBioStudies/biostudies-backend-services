package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.test.extSubmissionWithFileList
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.allFileListSections
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
class FilePersistenceServiceTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockSerializationService: SerializationService
) {
    private val sectionFile = temporaryFolder.createFile("file.txt", "text-1")
    private val fileList = temporaryFolder.createFile("fileList.json", "text-2")
    private val referencedFile = temporaryFolder.createFile("file2.txt", "text-3")
    private val extSubmission = extSubmissionWithFileList(listOf(sectionFile), fileList, listOf(referencedFile))
    private val relPath = extSubmission.relPath

    private val testInstance =
        FilePersistenceService(SubmissionFolderResolver(temporaryFolder.root.toPath()), mockSerializationService)

    @BeforeEach
    fun beforeEach() {
        val simpleSubmission = extSubmission.toSimpleSubmission()
        every { mockSerializationService.serializeElement(simpleSubmission, XML) } returns ""
        every { mockSerializationService.serializeElement(simpleSubmission, TSV) } returns ""
        every { mockSerializationService.serializeElement(simpleSubmission, JSON_PRETTY) } returns ""

        val sectionFileList = extSubmission.allFileListSections.first()
        every { mockSerializationService.serializeElement(sectionFileList, XML) } returns ""
        every { mockSerializationService.serializeElement(sectionFileList, TSV) } returns ""
        every { mockSerializationService.serializeElement(sectionFileList, JSON_PRETTY) } returns ""
    }

    @Test
    fun persistSubmissionFiles() {
        testInstance.persistSubmissionFiles(extSubmission)

        assertThat(getPath("$relPath/file.txt")).exists()
        assertThat(getPath("$relPath/file2.txt")).exists()

        assertThat(getPath("$relPath/ABC-123.xml")).exists()
        assertThat(getPath("$relPath/ABC-123.json")).exists()
        assertThat(getPath("$relPath/ABC-123.pagetab.tsv")).exists()

        assertThat(getPath("$relPath/fileList.xml")).exists()
        assertThat(getPath("$relPath/fileList.json")).exists()
        assertThat(getPath("$relPath/fileList.pagetab.tsv")).exists()
    }

    private fun getPath(path: String) = Paths.get("${temporaryFolder.root.absolutePath}/$path")
}
