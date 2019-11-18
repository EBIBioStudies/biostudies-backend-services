package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockMultipartFile

private const val FILE_CONTENT = "file-content"
private const val FILE_NAME = "file.txt"

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class TempFileGeneratorTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val properties: ApplicationProperties
) {

    private val testInstance = TempFileGenerator(properties)
    private val requestFile =
        MockMultipartFile(FILE_NAME, "request-folder/$FILE_NAME", "Text/plain", FILE_CONTENT.toByteArray())

    @BeforeAll
    fun beforeAll() {
        every { properties.tempDirPath } returns temporaryFolder.root.absolutePath
    }

    @Test
    fun asFiles() {
        val files = testInstance.asFiles(arrayOf(requestFile))

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
}
