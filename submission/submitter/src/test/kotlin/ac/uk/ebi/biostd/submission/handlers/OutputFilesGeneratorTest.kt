package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.integration.ISerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.paths.FolderResolver
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class OutputFilesGeneratorTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockFolderResolver: FolderResolver,
    @MockK private val mockSerializationService: ISerializationService
) {
    private val submission = createBasicExtendedSubmission()
    private val testInstance = OutputFilesGenerator(mockFolderResolver, mockSerializationService)

    @BeforeEach
    fun beforeEach() {
        every { mockSerializationService.serializeElement(submission, SubFormat.XML) } returns ""
        every { mockSerializationService.serializeElement(submission, SubFormat.TSV) } returns ""
        every { mockSerializationService.serializeElement(submission, SubFormat.JSON_PRETTY) } returns ""
        every { mockFolderResolver.getSubmissionFolder(submission) } returns temporaryFolder.root.toPath()
    }

    @Test
    fun `submission output files`() {
        testInstance.generate(submission)

        assertThat(Paths.get("${temporaryFolder.root.absolutePath}/ABC456.xml")).exists()
        assertThat(Paths.get("${temporaryFolder.root.absolutePath}/ABC456.json")).exists()
        assertThat(Paths.get("${temporaryFolder.root.absolutePath}/ABC456.pagetab.tsv")).exists()
    }
}
