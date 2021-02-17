package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFile
import ac.uk.ebi.biostd.persistence.doc.test.FileTestHelper.docFileList
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.assertExtSection
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ac.uk.ebi.biostd.persistence.doc.test.TEST_REL_PATH
import arrow.core.Either.Companion.left
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ToExtSectionTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_REL_PATH)
    private val testDocFile = docFile.copy(fullPath = testFile.absolutePath)
    private val testDocSection = docSection.copy(
        files = listOf(left(testDocFile)),
        fileList = docFileList.copy(files = listOf(testDocFile))
    )

    @Test
    fun `to ext section`() {
        val extSection = testDocSection.toExtSection()
        assertExtSection(extSection, testFile)
    }
}
