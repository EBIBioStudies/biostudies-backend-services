package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.File
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(value = [MockKExtension::class, TemporaryFolderExtension::class])
internal class ToExtFileTest(private val tempFolder: TemporaryFolder) {

    private val file = File("fileName", 55L, listOf(attribute))
    private val systemFile = tempFolder.createFile(file.path)

    @Test
    fun toExtFile() {
        val extFile = file.toExtFile(PathFilesSource(tempFolder.root.toPath()))

        assertThat(extFile.fileName).isEqualTo(file.path)
        assertThat(extFile.file).isEqualTo(systemFile)
        assertThat(extFile.attributes).hasSize(1)
        assertAttribute(extFile.attributes.first(), file.attributes.first())
    }
}
