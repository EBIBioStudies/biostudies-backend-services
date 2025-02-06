package uk.ac.ebi.io.sources

import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.model.constants.FileFields.FILE_TYPE
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.apache.commons.net.ftp.FTPFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.net.SocketTimeoutException
import java.nio.file.Path
import kotlin.io.path.name

@ExtendWith(MockKExtension::class)
class FtpSourceTest(
    @MockK private val file: File,
    @MockK private val ftpUrl: Path,
    @MockK private val ftpPath: Path,
    @MockK private val ftpParentPath: Path,
    @MockK private val nfsPath: Path,
    @MockK private val filePath: Path,
    @MockK private val ftpFile: FTPFile,
    @MockK private val ftpClient: FtpClient,
) {
    private val testInstance = FtpSource(DESCRIPTION, ftpUrl, nfsPath, ftpClient)

    @BeforeEach
    fun beforeEach() {
        every { ftpFile.size } returns 123L
        every { ftpFile.name } returns FILE_NAME
        every { ftpFile.isDirectory } returns false

        every { ftpPath.name } returns FILE_NAME
        every { ftpPath.parent } returns ftpParentPath

        every { filePath.toFile() } returns file
        every { file.absolutePath } returns FILE_PATH
        every { ftpUrl.resolve(FILE_NAME) } returns ftpPath
        every { nfsPath.resolve(FILE_NAME) } returns filePath
    }

    @Test
    fun `find file when connection fails`() =
        runTest {
            every {
                ftpClient.listFiles(ftpParentPath)
            } throws SocketTimeoutException() andThenAnswer { listOf(ftpFile) }

            val file = testInstance.getExtFile(FILE_NAME, FILE_TYPE.name, listOf())
            assertThat(file).isNotNull()
            assertThat(file!!.fileName).isEqualTo(FILE_NAME)
        }

    private companion object {
        const val DESCRIPTION = "test-source"
        const val FILE_NAME = "test-file"
        const val FILE_PATH = "/a/path/test-file"
    }
}
