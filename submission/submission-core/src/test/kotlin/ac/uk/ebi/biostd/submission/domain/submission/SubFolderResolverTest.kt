package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SubFolderResolverTest(
    @MockK(relaxed = true) private val properties: ApplicationProperties,
) {
    private val testInstance = SubFolderResolver(properties)

    @BeforeEach
    fun beforeEach() {
        every { properties.persistence.includeSecretKey } returns true
        every { properties.persistence.privateSubmissionsPath } returns "/tmp/nfs/submission/.private"
        every { properties.persistence.publicSubmissionsPath } returns "/tmp/nfs/submission"
    }

    @Nested
    inner class IncludingSecretKey {
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
        @BeforeEach
        fun beforeEach() {
            every { properties.persistence.includeSecretKey } returns false
            every { properties.persistence.privateSubmissionsPath } returns "/tmp/nfs/submission"
            every { properties.persistence.publicSubmissionsPath } returns "/tmp/nfs/submission/ftp"
        }

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
