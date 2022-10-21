package ac.uk.ebi.biostd.persistence.filesystem.fire

import ebi.ac.uk.extended.model.FireFile
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireClient

@ExtendWith(MockKExtension::class)
class FireFtpServiceTest(
    @MockK private val fireClient: FireClient,
) {
    private val testInstance = FireFtpService(fireClient)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `release submission file`(
        @MockK file: FireFile
    ) {
        every { file.fireId } returns "fire-id"
        every { fireClient.publish("fire-id") } answers { nothing }

        testInstance.releaseSubmissionFile(file, "/rel/path")

        verify(exactly = 1) { fireClient.publish("fire-id") }
    }
}
