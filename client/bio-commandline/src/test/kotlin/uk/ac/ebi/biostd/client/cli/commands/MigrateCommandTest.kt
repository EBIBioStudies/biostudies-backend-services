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
    @MockK private val submissionService: SubmissionService
) {
    private val testInstance = MigrateCommand(submissionService)

    @Test
    fun whenNoTargetOwner() {
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
                "-tp", "78910"
            )
        )

        val request = requestSlot.captured
        assertRequest(request)
        verify(exactly = 1) { submissionService.migrate(request) }
    }

    @Test
    fun whenTargetOwner() {
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
                "-to", "Juan"
            )
        )

        val request = requestSlot.captured
        assertRequest(request)
        assertThat(request.targetOwner).isEqualTo("Juan")
        verify(exactly = 1) { submissionService.migrate(request) }
    }

    private fun assertRequest(request: MigrationRequest) {
        assertThat(request.accNo).isEqualTo("S-BSST1")
        assertThat(request.source).isEqualTo("http://biostudy-prod.ebi.ac.uk")
        assertThat(request.sourceUser).isEqualTo("admin_user@ebi.ac.uk")
        assertThat(request.sourcePassword).isEqualTo("123456")
        assertThat(request.target).isEqualTo("http://biostudy-bia.ebi.ac.uk")
        assertThat(request.targetUser).isEqualTo("admin_user@ebi.ac.uk")
        assertThat(request.targetPassword).isEqualTo("78910")
    }
}
