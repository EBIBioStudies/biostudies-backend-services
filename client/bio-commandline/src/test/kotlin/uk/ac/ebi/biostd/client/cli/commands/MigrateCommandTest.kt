package uk.ac.ebi.biostd.client.cli.commands

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.services.SubmissionService

@ExtendWith(MockKExtension::class)
internal class MigrateCommandTest(
    @MockK private val submissionService: SubmissionService,
) {
    private val testInstance = MigrateCommand(submissionService)

    @Test
    fun `sync no target owner`() {
        val requestSlot = slot<MigrationRequest>()
        every { submissionService.migrate(capture(requestSlot)) } answers { nothing }

        testInstance.parse(
            listOf(
                "-ac", "S-BSST1",
                "-s", "http://biostudy-prod.ebi.ac.uk",
                "-su", "admin_user@ebi.ac.uk",
                "-sp", "123456",
                "-t", "http://biostudy-bia.ebi.ac.uk",
                "-tu", "admin_user@ebi.ac.uk",
                "-tp", "78910",
            ),
        )

        val request = requestSlot.captured
        assertRequest(request)
        assertThat(request.async).isFalse
        verify(exactly = 1) { submissionService.migrate(request) }
    }

    @Test
    fun `async with target owner`() {
        val requestSlot = slot<MigrationRequest>()
        every { submissionService.migrate(capture(requestSlot)) } answers { nothing }

        testInstance.parse(
            listOf(
                "-ac", "S-BSST1",
                "-s", "http://biostudy-prod.ebi.ac.uk",
                "-su", "admin_user@ebi.ac.uk",
                "-sp", "123456",
                "-t", "http://biostudy-bia.ebi.ac.uk",
                "-tu", "admin_user@ebi.ac.uk",
                "-tp", "78910",
                "-to", "Juan",
                "--async",
            ),
        )

        val request = requestSlot.captured
        assertRequest(request)
        assertThat(request.async).isTrue
        assertThat(request.targetOwner).isEqualTo("Juan")
        verify(exactly = 1) { submissionService.migrate(request) }
    }

    private fun assertRequest(request: MigrationRequest) {
        assertThat(request.accNo).isEqualTo("S-BSST1")
        assertThat(request.sourceSecurityConfig.server).isEqualTo("http://biostudy-prod.ebi.ac.uk")
        assertThat(request.sourceSecurityConfig.user).isEqualTo("admin_user@ebi.ac.uk")
        assertThat(request.sourceSecurityConfig.password).isEqualTo("123456")
        assertThat(request.targetSecurityConfig.server).isEqualTo("http://biostudy-bia.ebi.ac.uk")
        assertThat(request.targetSecurityConfig.user).isEqualTo("admin_user@ebi.ac.uk")
        assertThat(request.targetSecurityConfig.password).isEqualTo("78910")
    }
}
