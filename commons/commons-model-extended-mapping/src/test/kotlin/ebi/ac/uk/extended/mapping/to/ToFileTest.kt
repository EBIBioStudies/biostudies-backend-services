package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ToFileTest(
    @MockK val extAttribute: ExtAttribute,
    @MockK val attribute: Attribute,
    tempFolder: TemporaryFolder
) {
    private var systemFile = tempFolder.createFile("my-file", "test content")
    private val extFile = NfsFile("fileName", systemFile, listOf(extAttribute))

    @BeforeEach
    fun beforeEach() {
        mockkStatic(TO_ATTRIBUTE_EXTENSIONS)
        every { extAttribute.toAttribute() } returns attribute
    }

    @Test
    fun `from nfs file`() {
        assertFile(extFile.toFile())
    }

    @Test
    fun `from fire file`() {
        val fireFile = FireFile("fileName", "fireId", "md5", 12, listOf(extAttribute))
        assertFile(fireFile.toFile())
    }

    @Test
    fun `from fire directory`() {
        val fireDirectory = FireDirectory("fileName", "md5", 12, listOf(extAttribute))
        assertFile(fireDirectory.toFile())
    }

    private fun assertFile(file: File) {
        assertThat(file.attributes).containsExactly(attribute)
        assertThat(file.size).isEqualTo(12L)
        assertThat(file.path).isEqualTo(extFile.fileName)
    }
}
