package uk.ac.ebi.biostd.client.cli.commands

import com.github.ajalt.clikt.core.CliktError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.services.UserFilesService
import java.io.File

internal class UploadUserFilesCommandTest {
    private val service = mockk<UserFilesService>()
    private val testInstance = UploadUserFilesCommand(service)
    private val security = SecurityConfig("server", "user", "password")
    private val file = File("/shared/file.txt")
    private val args = listOf("-s", "server", "-u", "user", "-p", "password", "-f", file.path)

    @Test
    fun `HTTP remains default and requires no datamover configuration`() {
        coEvery { service.httpUpload(security, file, "") } returns Unit

        testInstance.parse(args)

        coVerify(exactly = 1) { service.httpUpload(security, file, "") }
        coVerify(exactly = 0) { service.datamoverUpload(any(), any(), any()) }
    }

    @Test
    fun `datamover flag routes to cluster transfer preserving relative destination`() {
        coEvery { service.datamoverUpload(security, file, "incoming") } returns Unit

        testInstance.parse(args + listOf("--datamover", "-rp", "incoming"))

        coVerify(exactly = 1) { service.datamoverUpload(security, file, "incoming") }
        coVerify(exactly = 0) { service.httpUpload(any(), any(), any()) }
    }

    @Test
    fun `datamover failure becomes CLI error retaining original cause`() {
        coEvery { service.datamoverUpload(security, file, "") } throws IllegalStateException("Job 12345 failed")

        val error = assertThrows<CliktError> { testInstance.parse(args + "--datamover") }

        assertThat(error).hasMessageContaining("Job 12345 failed")
        assertThat(error.cause).isInstanceOf(IllegalStateException::class.java)
    }
}
