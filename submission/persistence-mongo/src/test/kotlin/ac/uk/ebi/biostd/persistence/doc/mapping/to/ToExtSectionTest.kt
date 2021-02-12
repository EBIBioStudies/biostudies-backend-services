package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.assertExtSection
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ac.uk.ebi.biostd.persistence.doc.test.TEST_REL_PATH
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ToExtSectionTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_REL_PATH)

    @Test
    fun `to ext section`() {
        val extSection = docSection(testFile).toExtSection()
        assertExtSection(extSection, testFile)
    }
}
