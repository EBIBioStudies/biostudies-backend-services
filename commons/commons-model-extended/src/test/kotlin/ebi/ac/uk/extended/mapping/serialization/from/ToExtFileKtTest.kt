package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
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
internal class ToExtFileKtTest(
    @MockK val filesSource: FilesSource,
    @MockK val attribute: Attribute,
    @MockK val extAttribute: ExtAttribute,
    @MockK val systemFile: SystemFile
) {

    private val file = File("fileName", 55L, listOf(attribute))

    @Test
    fun toExtFile() {
        mockkStatic(TO_EXT_ATTRIBUTE_EXTENSIONS)

        every { attribute.toExtAttribute() } returns extAttribute
        every { filesSource.getFile(file.path) } returns systemFile

        val extFile = file.toExtFile(filesSource)
        assertThat(extFile.attributes).containsExactly(extAttribute)
        assertThat(extFile.fileName).isEqualTo(file.path)
        assertThat(extFile.file).isEqualTo(systemFile)
    }
}
