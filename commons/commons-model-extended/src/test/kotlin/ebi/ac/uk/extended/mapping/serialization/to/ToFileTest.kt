package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ToFileTest(
    @MockK val extAttribute: ExtAttribute,
    @MockK val attribute: Attribute,
    tempFolder: TemporaryFolder
) {
    private var systemFile = tempFolder.createFile("my-file", "test content")
    private val extFile = ExtFile("fileName", systemFile, listOf(extAttribute))

    @Test
    fun toExtFile() {
        mockkStatic(TO_ATTRIBUTE_EXTENSIONS) {
            every { extAttribute.toAttribute() } returns attribute

            val file = extFile.toFile()
            assertThat(file.attributes).containsExactly(attribute)
            assertThat(file.size).isEqualTo(12L)
            assertThat(file.path).isEqualTo(extFile.fileName)
        }
    }
}
