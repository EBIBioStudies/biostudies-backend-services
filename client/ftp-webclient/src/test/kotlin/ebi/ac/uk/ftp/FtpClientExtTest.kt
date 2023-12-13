package ebi.ac.uk.ftp

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.apache.commons.net.ftp.FTPFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths

@ExtendWith(MockKExtension::class)
class FtpClientExtTest(@MockK val testInstance: FtpClient) {

    @Test
    fun existsWhenExists(@MockK file: FTPFile) {
        val path = Paths.get("/dummy/path")
        every { testInstance.listFiles(path) } returns listOf(file)

        assertThat(testInstance.exists(path)).isTrue()
    }

    @Test
    fun existsWhenDoesNotExists() {
        val path = Paths.get("/dummy/path")
        every { testInstance.listFiles(path) } returns emptyList()

        assertThat(testInstance.exists(path)).isFalse()
    }
}
