package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.assertExtSection
import ac.uk.ebi.biostd.persistence.doc.test.SectionTestHelper.docSection
import ac.uk.ebi.biostd.persistence.doc.test.TEST_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.test.TEST_PATH
import ebi.ac.uk.io.sources.FilesSource
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class ToExtSectionTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val filesSource: FilesSource
) {
    private val testFile = temporaryFolder.createFile(TEST_PATH)

    @BeforeEach
    fun beforeEach() {
        every { filesSource.getFile(TEST_PATH) } returns testFile
    }

    @Test
    fun `to ext section`() {
        val extSection = docSection.toExtSection(filesSource)
        assertExtSection(extSection, testFile)
    }
}
