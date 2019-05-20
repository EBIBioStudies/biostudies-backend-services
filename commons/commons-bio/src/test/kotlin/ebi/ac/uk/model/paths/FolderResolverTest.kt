package ebi.ac.uk.model.paths

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
import ebi.ac.uk.paths.FolderResolver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class FolderResolverTest {
    private val testInstance = FolderResolver(Paths.get("/nfs/biostudies"), Paths.get("/nfs/biostudies/dropbox"))

    @Test
    fun `get submission folder`() {
        val submission =
            ExtendedSubmission("ABC-123", User(1L, "test@mail.com", "theSecret")).apply { relPath = "ABCxxx123/ABC-123" }

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

    @Test
    fun `get user magic folder`() {
        assertThat(testInstance
            .getUserMagicFolderPath(50L, "abc-123"))
            .isEqualTo(Paths.get("/nfs/biostudies/dropbox/ab/c-123-a50"))
    }

    @Test
    fun `get group magic folder`() {
        assertThat(testInstance
            .getGroupMagicFolderPath(100L, "def-456"))
            .isEqualTo(Paths.get("/nfs/biostudies/dropbox/de/f-456-b100"))
    }
}
