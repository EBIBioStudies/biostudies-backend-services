package ebi.ac.uk.paths

import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(MockKExtension::class)
internal class SubmissionFolderResolverTest {
    @Nested
    inner class IncludingSecretKey {
        private val testInstance =
            SubmissionFolderResolver(
                includeSecretKey = true,
                privateSubPath = Paths.get("/tmp/nfs/submission/.private"),
                publicSubPath = Paths.get("/tmp/nfs/submission"),
            )

        @Test
        fun `get private folder`() {
            val privatePath = testInstance.getPrivateSubFolder("secret-key", "part1/part2").toString()
            assertThat(privatePath).isEqualTo("/tmp/nfs/submission/.private/se/cret-key/part1/part2")
        }

        @Test
        fun `get public folder`() {
            val privatePath = testInstance.getPublicSubFolder("part1/part2").toString()
            assertThat(privatePath).isEqualTo("/tmp/nfs/submission/part1/part2")
        }
    }

    @Nested
    inner class NotIncludingSecretKey {
        private val testInstance =
            SubmissionFolderResolver(
                includeSecretKey = false,
                privateSubPath = Paths.get("/tmp/nfs/submission"),
                publicSubPath = Paths.get("/tmp/nfs/submission/ftp"),
            )

        @Test
        fun `get private folder`() {
            val privatePath = testInstance.getPrivateSubFolder("secret-key", "part1/part2").toString()
            assertThat(privatePath).isEqualTo("/tmp/nfs/submission/part1/part2")
        }

        @Test
        fun `get public folder`() {
            val privatePath = testInstance.getPublicSubFolder("part1/part2").toString()
            assertThat(privatePath).isEqualTo("/tmp/nfs/submission/ftp/part1/part2")
        }
    }
}
