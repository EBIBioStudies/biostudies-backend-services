package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.assertExtSubmission
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.docSubmission
import ac.uk.ebi.biostd.persistence.doc.test.TEST_REL_PATH
import arrow.core.Either.Companion.left
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ToExtSubmissionMapperTest(temporaryFolder: TemporaryFolder) {
    private val baseFolder = temporaryFolder.createDirectory("submissions")
    private val testFolder = baseFolder.createDirectory("S-TEST")
    private val innerFolder = testFolder.createDirectory("123")
    private val submissionFolder = innerFolder.createDirectory("S-TEST123")
    private val filesFolder = submissionFolder.createDirectory(FILES_DIR)
    private val sectionFile = filesFolder.createNewFile(TEST_REL_PATH)
    private val testInstance = ToExtSubmissionMapper()

    @Test
    fun `to ext Submission`() {
        val extSubmission = testInstance.toExtSubmission(testSubmission())
        assertExtSubmission(extSubmission, sectionFile)
    }

    private fun testSubmission(): DocSubmission {
        val testDocFile = docFile.copy(fullPath = sectionFile.absolutePath)

        return docSubmission.copy(
            section = docSection.copy(
                files = listOf(left(testDocFile)),
                fileList = docFileList.copy(files = listOf(DocFileRef(ObjectId())))
            )
        )
    }
}
