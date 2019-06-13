package ebi.ac.uk.model.paths

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
import ebi.ac.uk.paths.SubmissionFolderResolver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class FolderResolverTest {
    private val testInstance = SubmissionFolderResolver(Paths.get("/nfs/biostudies"))

    @Test
    fun `get submission folder`() {
        val submission = ExtendedSubmission("ABC-123", User(1L, "test@mail.com", "theSecret"))
            .apply { relPath = "ABCxxx123/ABC-123" }

        assertThat(testInstance
            .getSubmissionFolder(submission))
            .isEqualTo(Paths.get("/nfs/biostudies/submission/ABCxxx123/ABC-123"))
    }

    @Test
    fun `get submission file path`() {
        assertThat(testInstance
            .getSubFilePath("ABCxxx123/ABC-123", "File1.txt"))
            .isEqualTo(Paths.get("/nfs/biostudies/submission/ABCxxx123/ABC-123/Files/File1.txt"))
    }
}
