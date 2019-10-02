package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.io.sources.PathFilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

private const val GROUP_NAME = "A-Bio-Group"
private const val FILE_NAME = "myFile.txt"

@ExtendWith(MockKExtension::class)
internal class GroupSourceTest(@MockK private val pathFileSource: PathFilesSource) {
    private val testInstance = GroupSource(GROUP_NAME, pathFileSource)

    @Test
    fun exists() {
        every { pathFileSource.exists(FILE_NAME) } returns true

        assertThat(testInstance.exists("Groups/$GROUP_NAME/$FILE_NAME")).isTrue()
    }

    @Test
    fun getFile() {
        val file = File(FILE_NAME)
        every { pathFileSource.getFile(FILE_NAME) } returns file

        assertThat(testInstance.getFile("Groups/$GROUP_NAME/$FILE_NAME")).isEqualTo(file)
    }

    @Test
    fun size() {
        val fileSize = 55L
        every { pathFileSource.size(FILE_NAME) } returns fileSize

        assertThat(testInstance.size("Groups/$GROUP_NAME/$FILE_NAME")).isEqualTo(fileSize)
    }

    @Test
    fun readText() {
        val fileText = "file content"
        every { pathFileSource.readText(FILE_NAME) } returns fileText

        assertThat(testInstance.readText("Groups/$GROUP_NAME/$FILE_NAME")).isEqualTo(fileText)
    }
}
