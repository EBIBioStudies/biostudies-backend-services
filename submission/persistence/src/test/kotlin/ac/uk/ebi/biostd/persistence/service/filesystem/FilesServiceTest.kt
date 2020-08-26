package ac.uk.ebi.biostd.persistence.service.filesystem

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.test.extSubmissionWithFileList
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.FileMode.MOVE
import ebi.ac.uk.io.ALL_CAN_READ
import ebi.ac.uk.io.ALL_CAN_READ_NO_EXE
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.READ_ONLY_GROUP
import ebi.ac.uk.io.READ_ONLY_GROUP_NO_EXE
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.clean
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class FilesServiceTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockSerializationService: SerializationService
) {
    private lateinit var extSubmission: ExtSubmission

    private val testInstance =
        FilesService(SubmissionFolderResolver(
            Paths.get("${temporaryFolder.root.toPath()}/submission"),
            Paths.get("${temporaryFolder.root.toPath()}/ftp")
        ), mockSerializationService)

    @BeforeEach
    fun beforeEach() {
        temporaryFolder.clean()

        val file1 = temporaryFolder.createFile("file.txt", "text-1")
        val file2 = temporaryFolder.createFile("file2.txt", "text-3")
        val sectionFolder = temporaryFolder.createDirectory("fileDirectory")
        extSubmission = extSubmissionWithFileList(listOf(file1, file1, sectionFolder), listOf(file2, file1))

        val simpleSubmission = extSubmission.toSimpleSubmission()
        sectionFolder.createNewFile("file3.txt", "folder-file-content")

        every { mockSerializationService.serializeElement(simpleSubmission, XML) } returns ""
        every { mockSerializationService.serializeElement(simpleSubmission, TSV) } returns ""
        every { mockSerializationService.serializeElement(simpleSubmission, JSON_PRETTY) } returns ""

        every { mockSerializationService.serializeElement(any<FilesTable>(), XML) } returns ""
        every { mockSerializationService.serializeElement(any<FilesTable>(), TSV) } returns ""
        every { mockSerializationService.serializeElement(any<FilesTable>(), JSON_PRETTY) } returns ""
    }

    @Nested
    inner class WhenMove {
        @Test
        fun whenReleased() {
            testPersistSubmissionFiles(
                extSubmission.copy(released = true),
                MOVE,
                expectedFolderPermissions = ALL_CAN_READ,
                expectedFilePermissions = ALL_CAN_READ_NO_EXE
            )
        }

        @Test
        fun whenNotReleased() {
            testPersistSubmissionFiles(
                extSubmission.copy(released = false),
                MOVE,
                expectedFolderPermissions = READ_ONLY_GROUP,
                expectedFilePermissions = READ_ONLY_GROUP_NO_EXE
            )
        }
    }

    @Nested
    inner class WhenCopy {
        @Test
        fun whenReleased() {
            testPersistSubmissionFiles(
                extSubmission.copy(released = true),
                COPY,
                expectedFolderPermissions = ALL_CAN_READ,
                expectedFilePermissions = ALL_CAN_READ_NO_EXE
            )
        }

        @Test
        fun whenNotReleased() {
            testPersistSubmissionFiles(
                extSubmission.copy(released = false),
                COPY,
                expectedFolderPermissions = READ_ONLY_GROUP,
                expectedFilePermissions = READ_ONLY_GROUP_NO_EXE
            )
        }
    }

    private fun testPersistSubmissionFiles(
        extSubmission: ExtSubmission,
        mode: FileMode,
        expectedFilePermissions: Set<PosixFilePermission>,
        expectedFolderPermissions: Set<PosixFilePermission>
    ) {
        testInstance.persistSubmissionFiles(extSubmission, mode)

        val relPath = extSubmission.relPath
        val submissionFolder = getPath("submission/$relPath")
        assertFile(submissionFolder, expectedFolderPermissions)
        assertFile(submissionFolder.parent, ALL_CAN_READ)

        assertFile(getPath("submission/$relPath/Files"), expectedFolderPermissions)
        assertFile(getPath("submission/$relPath/Files/file.txt"), expectedFilePermissions)
        assertFile(getPath("submission/$relPath/Files/file2.txt"), expectedFilePermissions)

        val directoryPath = getPath("submission/$relPath/Files/fileDirectory")
        val directory = directoryPath.toFile()
        assertThat(FileUtils.listFiles(directory).first()).hasContent("folder-file-content")
        assertThat(FileUtils.listFiles(directory).first()).hasName("file3.txt")
        assertThat(Files.getPosixFilePermissions(directoryPath)).isEqualTo(expectedFolderPermissions)

        assertFile(getPath("submission/$relPath/ABC-123.xml"), expectedFilePermissions)
        assertFile(getPath("submission/$relPath/ABC-123.json"), expectedFilePermissions)
        assertFile(getPath("submission/$relPath/ABC-123.pagetab.tsv"), expectedFilePermissions)

        assertFile(getPath("submission/$relPath/fileList.xml"), expectedFilePermissions)
        assertFile(getPath("submission/$relPath/fileList.json"), expectedFilePermissions)
        assertFile(getPath("submission/$relPath/fileList.pagetab.tsv"), expectedFilePermissions)
    }

    private fun getPath(path: String) = Paths.get("${temporaryFolder.root.absolutePath}/$path")

    private fun assertFile(path: Path, expectedPermissions: Set<PosixFilePermission>) {
        assertThat(path).exists()
        assertThat(Files.getPosixFilePermissions(path)).containsExactlyInAnyOrderElementsOf(expectedPermissions)
    }
}
