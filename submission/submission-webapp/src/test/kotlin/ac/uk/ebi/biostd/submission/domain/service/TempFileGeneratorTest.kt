package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockMultipartFile
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

private const val FILE_CONTENT = "file-content"
private const val FILE_NAME = "file.txt"

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class TempFileGeneratorTest(
    private val temporaryFolder: TemporaryFolder,
) {
    private val clock = mockk<Clock>()
    private val testInstance = TempFileGenerator(temporaryFolder.root.absolutePath, clock)

    private val requestFile =
        MockMultipartFile(FILE_NAME, "request-folder/$FILE_NAME", "Text/plain", FILE_CONTENT.toByteArray())

    @Test
    fun asFiles() {
        val files = testInstance.asFiles(listOf(requestFile))

        assertThat(files).hasSize(1)

        val file = files.first()
        assertThat(file.readText()).isEqualTo(FILE_CONTENT)
        assertThat(file.name).isEqualTo(FILE_NAME)
    }

    @Test
    fun asFile() {
        val file = testInstance.asFile(requestFile)
        assertThat(file.readText()).isEqualTo(FILE_CONTENT)
        assertThat(file.name).isEqualTo(FILE_NAME)
    }

    @Test
    fun deleteOldFiles() {
        val f1Time = Instant.parse("2050-01-15T00:00:00Z") // 01/15/2050
        val f2Time = Instant.parse("2050-02-15T00:00:00Z") // 02/15/2050
        val f3Time = Instant.parse("2050-03-15T00:00:00Z") // 03/15/2050
        val f4Time = Instant.parse("2050-04-15T00:00:00Z") // 04/15/2050
        val f5Time = Instant.parse("2050-05-15T00:00:00Z") // 05/15/2050

        every { clock.instant() } returnsMany listOf(f1Time, f2Time, f3Time, f4Time, f5Time, f5Time)
        every { clock.zone } returns ZoneId.of("UTC")
        repeat(5) { testInstance.asFile(requestFile) }

        testInstance.deleteOldFiles(2)

        val resultFiles =
            temporaryFolder.root
                .listFiles()
                .orEmpty()
                .map { it.name }
        assertThat(resultFiles).containsExactlyInAnyOrder("20503", "20504", "20505")
    }
}
