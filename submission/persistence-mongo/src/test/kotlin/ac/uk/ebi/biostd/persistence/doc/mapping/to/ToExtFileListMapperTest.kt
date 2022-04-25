package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.TEST_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.test.fireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.test.fireDocFile
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
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
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
@Disabled
class ToExtFileListMapperTest(temporaryFolder: TemporaryFolder) {
    private val fileListDocFileRepository: FileListDocFileRepository = mockk()
    private val testInstance = ToExtFileListMapper(fileListDocFileRepository, TODO(), TODO())
    private val fileNfs = temporaryFolder.createDirectory("folder").createNewFile("nfsFileFile.txt")
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
    fun `toExtFileList including FileListFiles`() {
        every {
            fileListDocFileRepository.findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(
                "S-TEST123",
                1,
                "file-list"
            )
        } returns listOf(fileListDocFile)
        val fileList = docFileList.copy(pageTabFiles = listOf(fireDocFile, fireDocDirectory, nfsDocFile))

        val extFileList = testInstance.toExtFileList(fileList, "S-TEST123", 1, true)

        assertThat(extFileList.filePath).isEqualTo(TEST_FILE_LIST)
        // assertThat(extFileList.files).hasSize(1)
        // assertThat(extFileList.files.first()).isEqualTo(fireDocFile.toExtFile())
        assertPageTabs(extFileList.pageTabFiles)
    }

    @Test
    fun `toExtFileList without FileListFiles`() {
        val fileList = docFileList.copy(pageTabFiles = listOf(fireDocFile, fireDocDirectory, nfsDocFile))

        val extFileList = testInstance.toExtFileList(fileList, "S-TEST123", 1, false)

        assertThat(extFileList.filePath).isEqualTo(TEST_FILE_LIST)
        // assertThat(extFileList.files).hasSize(0)
        assertPageTabs(extFileList.pageTabFiles)
        verify { fileListDocFileRepository wasNot called }
    }

    private fun assertPageTabs(pageTabFiles: List<ExtFile>) {
        assertThat(pageTabFiles).hasSize(3)
        assertThat(pageTabFiles.first()).isEqualTo(fireDocFile.toExtFile())
        assertThat(pageTabFiles.second()).isEqualTo(fireDocDirectory.toExtFile())
        assertThat(pageTabFiles.third()).isEqualTo(nfsDocFile.toExtFile())
    }
}
