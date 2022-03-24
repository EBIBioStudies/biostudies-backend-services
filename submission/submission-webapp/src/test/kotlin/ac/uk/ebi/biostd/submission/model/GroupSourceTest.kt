package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.io.sources.PathFilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val GROUP_NAME = "A-Bio-Group"
private const val FILE_NAME = "myFile.txt"

@ExtendWith(MockKExtension::class)
internal class GroupSourceTest(
    @MockK private val pathFileSource: PathFilesSource,
    @MockK private val file: NfsBioFile,
) {
    private val testInstance = GroupSource(GROUP_NAME, pathFileSource)

    @Test
    fun exists() {
        every { pathFileSource.getFile(FILE_NAME) } returns file

        assertThat(testInstance.getFile("groups/$GROUP_NAME/$FILE_NAME")).isEqualTo(file)
    }
}
