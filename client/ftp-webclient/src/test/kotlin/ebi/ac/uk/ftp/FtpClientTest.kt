package ebi.ac.uk.ftp

import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.verify
import org.apache.commons.net.ftp.FTPClient
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
        mockkConstructor(FTPClient::class)
        every { anyConstructed<FTPClient>().makeDirectory(any()) } answers { true }
        every { anyConstructed<FTPClient>().connect("ftp-dummy.uk", 678) } answers { nothing }
        every { anyConstructed<FTPClient>().login("user", "password") } answers { true }

        testInstance.createFolder(Path("zero/one/two"))

        verify(exactly = 1) { anyConstructed<FTPClient>().makeDirectory("zero") }
        verify(exactly = 1) { anyConstructed<FTPClient>().makeDirectory("zero/one") }
        verify(exactly = 1) { anyConstructed<FTPClient>().makeDirectory("zero/one/two") }
    }
}
