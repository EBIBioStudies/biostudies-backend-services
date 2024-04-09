package uk.ac.ebi.scheduler.pmc.exporter.cli

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPConnectionClosedException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.scheduler.pmc.exporter.config.ApplicationProperties
import uk.ac.ebi.scheduler.pmc.exporter.config.Ftp
import java.io.InputStream

@ExtendWith(MockKExtension::class)
class BioStudiesFtpClientTest(
    @MockK private val ftpClient: FTPClient,
    @MockK private val appProperties: ApplicationProperties,
) {
    private val testInstance = BioStudiesFtpClient(ftpClient, appProperties)

    @BeforeEach
    fun beforeEach() {
        setUpFtpClient()
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun login() {
        testInstance.login()

        verify(exactly = 1) {
            ftpClient.login("test", "admin")
            ftpClient.connect("localhost", 21)
            ftpClient.enterLocalPassiveMode()
        }
    }

    @Test
    fun logout() {
        testInstance.logout()

        verify(exactly = 1) {
            ftpClient.logout()
            ftpClient.disconnect()
        }
    }

    @Test
    fun `store file`(
        @MockK content: InputStream,
    ) {
        every { ftpClient.storeFile("a/test/path", content) } returns true

        testInstance.storeFile("a/test/path", content)

        verify(exactly = 1) { ftpClient.storeFile("a/test/path", content) }
    }

    @Test
    fun `attempt reconnection after failure`(
        @MockK content: InputStream,
    ) {
        every {
            ftpClient.storeFile("a/test/path", content)
        } throws FTPConnectionClosedException("Error") andThenAnswer { true }

        testInstance.storeFile("a/test/path", content)

        verify(exactly = 2) { ftpClient.storeFile("a/test/path", content) }
        verify(exactly = 1) {
            ftpClient.disconnect()
            ftpClient.login("test", "admin")
            ftpClient.connect("localhost", 21)
            ftpClient.enterLocalPassiveMode()
        }
    }

    private fun setUpFtpClient() {
        val ftpProperties =
            Ftp().apply {
                host = "localhost"
                user = "test"
                password = "admin"
                port = 21
            }

        every { ftpClient.logout() } returns true
        every { appProperties.ftp } returns ftpProperties
        every { ftpClient.disconnect() } answers { nothing }
        every { ftpClient.login("test", "admin") } returns true
        every { ftpClient.enterLocalPassiveMode() } answers { nothing }
        every { ftpClient.connect("localhost", 21) } answers { nothing }
    }
}
