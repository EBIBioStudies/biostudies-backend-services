package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.fireDocFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.nfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.assertExtSubmission
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.docSubmission
import ac.uk.ebi.biostd.persistence.doc.test.TEST_FILENAME
import arrow.core.Either.Companion.left
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import ac.uk.ebi.biostd.persistence.doc.test.fireDocDirectory as subFireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.test.fireDocFile as subFireDocFile

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class ToExtSubmissionMapperTest(private val temporaryFolder: TemporaryFolder) {
    private val baseFolder = temporaryFolder.createDirectory("submissions")
    private val testFolder = baseFolder.createDirectory("S-TEST")
    private val innerFolder = testFolder.createDirectory("123")
    private val submissionFolder = innerFolder.createDirectory("S-TEST123")
    private val filesFolder = submissionFolder.createDirectory(FILES_DIR)
    private val sectionFile = filesFolder.createNewFile(TEST_FILENAME)
    private val listFilesRepo = mockk<FileListDocFileRepository>()
    private val testInstance = ToExtSubmissionMapper(listFilesRepo)
    private val fileNfs = temporaryFolder.createDirectory("folder").createNewFile("nfsFileFile.txt")

    @Test
    fun `to ext Submission without FileListFiles`() {
        val extSubmission = testInstance.toExtSubmission(docSubmission(), includeFileListFiles = false)

        assertExtSubmission(extSubmission, sectionFile, fileNfs, includeFileListFiles = false)
    }

    @Test
    fun `to ext Submission including FileListFiles`() {
        every {
            listFilesRepo.findAllBySubmissionAccNoAndSubmissionVersionAndFileListName("S-TEST123", 1, "file-list.tsv")
        } returns listOf(fileListDocFile)
        val extSubmission = testInstance.toExtSubmission(docSubmission(), includeFileListFiles = true)

        assertExtSubmission(extSubmission, sectionFile, fileNfs, includeFileListFiles = true)
    }

    private val fileListDocFile = FileListDocFile(
        id = ObjectId(),
        submissionId = ObjectId(),
        file = fireDocFile,
        fileListName = "file-list.tsv",
        index = 0,
        submissionVersion = 1,
        submissionAccNo = "S-TEST123"
    )

    private fun docSubmission(): DocSubmission {
        val testNfsDocFile =
            nfsDocFile.copy(fullPath = sectionFile.absolutePath, md5 = sectionFile.md5(), fileSize = sectionFile.size())
        val testFireDocFile = fireDocFile
        val subNfsDocFile =
            NfsDocFile(
                fileNfs.name,
                "filePath",
                "relPath",
                fileNfs.absolutePath,
                listOf(),
                fileNfs.md5(),
                fileNfs.size(),
                "file"
            )

        return docSubmission.copy(
            section = docSection.copy(
                files = listOf(left(testNfsDocFile), left(testFireDocFile)),
                fileList = docFileList
            ),
            pageTabFiles = listOf(subFireDocFile, subFireDocDirectory, subNfsDocFile)
        )
    }
}
