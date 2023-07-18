package ebi.ac.uk.ftp

import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.verify
import org.apache.commons.net.ftp.FTPSClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class FtpClientTest {

    private val testInstance = FtpClient(
        ftpUser = "user",
        ftpPassword = "password",
        ftpUrl = "ftp-dummy.uk",
        ftpPort = 678
    )

    @Test
    fun createFolder() {
        mockkConstructor(FTPSClient::class)
        every { anyConstructed<FTPSClient>().makeDirectory(any()) } answers { true }
        every { anyConstructed<FTPSClient>().connect("ftp-dummy.uk", 678) } answers { nothing }
        every { anyConstructed<FTPSClient>().login("user", "password") } answers { true }
        every { anyConstructed<FTPSClient>().logout() } answers { true }
        every { anyConstructed<FTPSClient>().disconnect() } answers { nothing }

        testInstance.createFolder(Path("zero/one/two"))

        verify(exactly = 1) { anyConstructed<FTPSClient>().makeDirectory("zero") }
        verify(exactly = 1) { anyConstructed<FTPSClient>().makeDirectory("zero/one") }
        verify(exactly = 1) { anyConstructed<FTPSClient>().makeDirectory("zero/one/two") }
    }

    @Test
    fun listFiles() {
        mockkConstructor(FTPSClient::class)
        every { anyConstructed<FTPSClient>().changeWorkingDirectory("zero/one/two with spaces") } answers { true }
        every { anyConstructed<FTPSClient>().listFiles() } answers { emptyArray() }
        every { anyConstructed<FTPSClient>().connect("ftp-dummy.uk", 678) } answers { nothing }
        every { anyConstructed<FTPSClient>().login("user", "password") } answers { true }
        every { anyConstructed<FTPSClient>().logout() } answers { true }
        every { anyConstructed<FTPSClient>().disconnect() } answers { nothing }

        val result = testInstance.listFiles(Path("zero/one/two with spaces"))

        assertThat(result).isEmpty()
    }
}
