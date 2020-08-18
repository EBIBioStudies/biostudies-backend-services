package ebi.ac.uk.paths

import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(MockKExtension::class)
internal class SubmissionFolderResolverTest {
    private val testInstance = SubmissionFolderResolver(
        Paths.get("/tmp/nfs/submission"),
        Paths.get("/tmp/nfs/submission/ftp")
    )

    @Test
    fun getSubmissionFolder() {
        val submissionPath = "part1/part2"

        assertThat(testInstance.getSubFolder(submissionPath).toString())
            .isEqualTo("/tmp/nfs/submission/part1/part2")
    }

    @Test
    fun getSubFilePath() {
        val submissionPath = "part1/part2"

        assertThat(testInstance.getSubFilePath(submissionPath, "/test-file.txt").toString())
            .isEqualTo("/tmp/nfs/submission/part1/part2/Files/test-file.txt")
    }
}
