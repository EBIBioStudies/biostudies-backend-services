package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.RW_R__R__
import ebi.ac.uk.model.Submission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.nio.file.Files

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class PageTabUtilTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val serializationService: SerializationService,
    @MockK private val toSubmissionMapper: ToSubmissionMapper,
    @MockK private val fileListMapper: ToFileListMapper,
    @MockK private val extSubmission: ExtSubmission,
    @MockK private val submission: Submission,
) {
    private val testInstance = PageTabUtil(serializationService, toSubmissionMapper, fileListMapper)

    @Test
    fun `generate submission pagetab`() = runTest {
        every { extSubmission.released } returns true
        every { extSubmission.accNo } returns "S-BSST1"
        every { serializationService.serializeSubmission(submission, TSV) } returns "tsv-sub"
        every { serializationService.serializeSubmission(submission, JSON_PRETTY) } returns "json-sub"
        coEvery { toSubmissionMapper.toSimpleSubmission(extSubmission) } returns submission

        val pageTabFiles = testInstance.generateSubPageTab(extSubmission, tempFolder.root)
        coVerify(exactly = 1) { toSubmissionMapper.toSimpleSubmission(extSubmission) }
        assertPageTabFile(pageTabFiles.tsv, "tsv-sub")
        assertPageTabFile(pageTabFiles.json, "json-sub")
    }

    private fun assertPageTabFile(file: File, content: String) {
        assertThat(file.exists()).isTrue()
        assertThat(file.readText()).isEqualTo(content)
        assertThat(Files.getPosixFilePermissions(file.toPath())).isEqualTo(RW_R__R__)
    }
}
