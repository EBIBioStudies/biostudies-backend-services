package ac.uk.ebi.biostd.persistence.nfs

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.persistence.filesystem.extSubmissionWithFileList
import ac.uk.ebi.biostd.persistence.filesystem.nfs.NfsFilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.FileMode.MOVE
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXR_XR_X
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.io.ext.createNewFile
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
import java.nio.file.Files.getPosixFilePermissions
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class NfsFilesServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val mockSerializationService: SerializationService
) {
    private lateinit var extSubmission: ExtSubmission

    private val rootPath = tempFolder.root.toPath()
    private val folderResolver = SubmissionFolderResolver(Paths.get("$rootPath/submission"), Paths.get("$rootPath/ftp"))
    private val testInstance = NfsFilesService(folderResolver)

    @BeforeEach
    fun beforeEach() {
        tempFolder.clean()

        val file1 = tempFolder.createFile("file.txt", "text-1")
        val file2 = tempFolder.createFile("file2.txt", "text-3")
        val sectionFolder = tempFolder.createDirectory("fileDirectory")
        extSubmission = extSubmissionWithFileList(listOf(file1, file1, sectionFolder), listOf(file2, file1))

        val simpleSubmission = extSubmission.toSimpleSubmission()
        sectionFolder.createNewFile("file3.txt", "folder-file-content")

        every { mockSerializationService.serializeSubmission(simpleSubmission, XML) } returns ""
        every { mockSerializationService.serializeSubmission(simpleSubmission, TSV) } returns ""
        every { mockSerializationService.serializeSubmission(simpleSubmission, JSON_PRETTY) } returns ""

        //every { mockSerializationService.serializeFileList(any(), XML) } returns ""
        //every { mockSerializationService.serializeFileList(any(), TSV) } returns ""
        //every { mockSerializationService.serializeFileList(any(), JSON_PRETTY) } returns ""
    }

    @Nested
    inner class WhenMove {
        @Test
        fun whenReleased() {
            testPersistSubmissionFiles(extSubmission.copy(released = true), MOVE, RW_R__R__, RWXR_XR_X)
        }

        @Test
        fun whenNotReleased() {
            testPersistSubmissionFiles(extSubmission.copy(released = false), MOVE, RW_R_____, RWXR_X___)
        }
    }

    @Nested
    inner class WhenCopy {
        @Test
        fun whenReleased() {
            testPersistSubmissionFiles(extSubmission.copy(released = true), COPY, RW_R__R__, RWXR_XR_X)
        }

        @Test
        fun whenNotReleased() {
            testPersistSubmissionFiles(extSubmission.copy(released = false), COPY, RW_R_____, RWXR_X___)
        }
    }

    private fun testPersistSubmissionFiles(
        extSubmission: ExtSubmission,
        mode: FileMode,
        expectedFilePermissions: Set<PosixFilePermission>,
        expectedFolderPermissions: Set<PosixFilePermission>
    ) {
        testInstance.persistSubmissionFiles(FilePersistenceRequest(extSubmission, mode))

        val relPath = extSubmission.relPath

        val submissionFolder = getPath("submission/$relPath")
        assertFile(submissionFolder, expectedFolderPermissions)
        assertFile(submissionFolder.parent, RWXR_XR_X)

        assertFile(getPath("submission/$relPath/Files"), expectedFolderPermissions)
        assertFile(getPath("submission/$relPath/Files/file.txt"), expectedFilePermissions)
        assertFile(getPath("submission/$relPath/Files/file2.txt"), expectedFilePermissions)

        val directoryPath = getPath("submission/$relPath/Files/fileDirectory")
        val directory = directoryPath.toFile()
        assertThat(FileUtils.listFiles(directory).first()).hasContent("folder-file-content")
        assertThat(FileUtils.listFiles(directory).first()).hasName("file3.txt")
        assertThat(getPosixFilePermissions(directoryPath)).isEqualTo(expectedFolderPermissions)
    }

    private fun getPath(path: String) = Paths.get("${tempFolder.root.absolutePath}/$path")

    private fun assertFile(path: Path, expectedPermissions: Set<PosixFilePermission>) {
        assertThat(path).exists()
        assertThat(getPosixFilePermissions(path)).containsExactlyInAnyOrderElementsOf(expectedPermissions)
    }
}
