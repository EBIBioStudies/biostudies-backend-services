package ebi.ac.uk.paths

import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(MockKExtension::class)
internal class SubmissionFolderResolverTest(@MockK val submission: ExtSubmission) {
    private val testInstance = SubmissionFolderResolver(Paths.get("/tmp/nfs"))

    @Test
    fun getSubmissionFolder() {
        val submissionPath = "part1/part2"
        every { submission.relPath } returns submissionPath

        assertThat(testInstance.getSubmissionFolder(submission).toString())
            .isEqualTo("/tmp/nfs/submission/part1/part2")
    }

    @Test
    fun getSubFilePath() {
        val submissionPath = "part1/part2"
        every { submission.relPath } returns submissionPath

        assertThat(testInstance.getSubFilePath(submissionPath, "/test-file.txt").toString())
            .isEqualTo("/tmp/nfs/submission/part1/part2/Files/test-file.txt")
    }
}
