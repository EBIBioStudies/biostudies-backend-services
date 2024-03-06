package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.data.FileListDocFileDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.TEST_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.test.fireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.test.fireDocFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.newFile
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.called
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.files
import uk.ac.ebi.serialization.common.FilesResolver

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class ToExtFileListMapperTest(temporaryFolder: TemporaryFolder) {
    private val fileListDocFileRepository: FileListDocFileDocDataRepository = mockk()
    private val extSerializationService = ExtSerializationService()
    private val testInstance = ToExtFileListMapper(
        fileListDocFileRepository,
        extSerializationService,
        FilesResolver(temporaryFolder.createDirectory("ext-files"))
    )
    private val fileNfs = temporaryFolder.createDirectory("folder").newFile("nfsFileFile.txt")
    private val nfsDocFile = NfsDocFile(
        fileNfs.name,
        "filePath",
        "relPath",
        fileNfs.absolutePath,
        listOf(),
        fileNfs.md5(),
        fileNfs.size(),
        "file"
    )
    private val fileListDocFile = FileListDocFile(
        id = ObjectId(),
        submissionId = ObjectId(),
        file = fireDocFile,
        fileListName = "file-list",
        index = 0,
        submissionVersion = 1,
        submissionAccNo = "S-TEST123"
    )

    @Test
    fun `toExtFileList including FileListFiles`() = runTest {
        every {
            fileListDocFileRepository.findByFileList(
                "S-TEST123",
                1,
                "file-list"
            )
        } returns flowOf(fileListDocFile)
        val fileList = docFileList.copy(pageTabFiles = listOf(fireDocFile, fireDocDirectory, nfsDocFile))

        val extFileList = testInstance.toExtFileList(fileList, "S-TEST123", 1, "SubRelPath", true)

        assertThat(extFileList.filePath).isEqualTo(TEST_FILE_LIST)
        assertPageTabs(extFileList.pageTabFiles)

        val files = extSerializationService.files(extFileList.file)
        assertThat(files).hasSize(1)
        assertThat(files.first()).isEqualTo(fireDocFile.toExtFile("SubRelPath"))
    }

    @Test
    fun `toExtFileList without FileListFiles`() = runTest {
        val fileList = docFileList.copy(pageTabFiles = listOf(fireDocFile, fireDocDirectory, nfsDocFile))

        val extFileList = testInstance.toExtFileList(fileList, "S-TEST123", 1, "SubRelPath", false)

        assertThat(extFileList.filePath).isEqualTo(TEST_FILE_LIST)
        assertThat(extSerializationService.files(extFileList.file)).isEmpty()
        assertPageTabs(extFileList.pageTabFiles)
        verify { fileListDocFileRepository wasNot called }
    }

    private fun assertPageTabs(pageTabFiles: List<ExtFile>) {
        assertThat(pageTabFiles).hasSize(3)
        assertThat(pageTabFiles.first()).isEqualTo(fireDocFile.toExtFile("SubRelPath"))
        assertThat(pageTabFiles.second()).isEqualTo(fireDocDirectory.toExtFile("SubRelPath"))
        assertThat(pageTabFiles.third()).isEqualTo(nfsDocFile.toExtFile("SubRelPath"))
    }
}
