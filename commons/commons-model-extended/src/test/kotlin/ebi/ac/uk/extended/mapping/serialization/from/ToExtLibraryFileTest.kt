package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.File
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.utils.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File as SystemFile

@ExtendWith(MockKExtension::class)
internal class ToExtLibraryFileTest(
    @MockK val fileSource: FilesSource,
    @MockK val file: File,
    @MockK val systemFile: SystemFile,
    @MockK val extFile: ExtFile
) {

    private val libraryFile = LibraryFile("libraryFile", listOf(file))

    @Test
    fun toExtLibraryFile() {
        mockkStatic(TO_EXT_FILE_EXTENSIONS)
        every { file.toExtFile(fileSource) } returns extFile
        every { fileSource.getFile(libraryFile.name) } returns systemFile

        val extLibraryFile = libraryFile.toExtLibraryFile(fileSource)
        assertThat(extLibraryFile.referencedFiles.first()).isEqualTo(extFile)
        assertThat(extLibraryFile.fileName).isEqualTo(libraryFile.name)
        assertThat(extLibraryFile.file).isEqualTo(systemFile)
    }
}
