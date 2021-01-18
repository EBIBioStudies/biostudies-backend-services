package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.assertExtSubmission
import ac.uk.ebi.biostd.persistence.doc.test.SubmissionTestHelper.docSubmission
import ac.uk.ebi.biostd.persistence.doc.test.TEST_PATH
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ToExtSubmissionMapperTest(temporaryFolder: TemporaryFolder) {
    private val baseFolder = temporaryFolder.createDirectory("submissions")
    private val testFolder = baseFolder.createDirectory("S-TEST")
    private val innerFolder = testFolder.createDirectory("123")
    private val submissionFolder = innerFolder.createDirectory("S-TEST123")
    private val filesFolder = submissionFolder.createDirectory(FILES_DIR)
    private val sectionFile = filesFolder.createNewFile(TEST_PATH)
    private val testInstance = ToExtSubmissionMapper(baseFolder.toPath())

    @Test
    fun `to ext Submission`() {
        val extSubmission = testInstance.toExtSubmission(docSubmission)
        assertExtSubmission(extSubmission, sectionFile)
    }
}
