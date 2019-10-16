package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.attribute
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.extTestAttribute2
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.fileAttribute
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.refAttribute
import ac.uk.ebi.biostd.persistence.model.File
import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ebi.ac.uk.io.sources.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File as SystemFile

@ExtendWith(MockKExtension::class)
internal class ToExtFileTest(
    @MockK val filesSource: FilesSource,
    @MockK val systemFile: SystemFile
) {
    @Test
    fun `File to ExtFile`() {
        val file = File("fileName", 1, 55L, sortedSetOf(fileAttribute))

        mockkStatic(TO_EXT_ATTRIBUTE_EXTENSIONS) {
            every { attribute.toExtAttribute() } returns extTestAttribute2
            every { filesSource.getFile(file.name) } returns systemFile

            val extFile = file.toExtFile(filesSource)
            assertThat(extFile.fileName).isEqualTo(file.name)
            assertThat(extFile.file).isEqualTo(systemFile)
            assertThat(extFile.attributes).containsOnly(extTestAttribute2)
        }
    }

    @Test
    fun `ReferencedFile to ExtFile`() {
        val refFile = ReferencedFile("fileName", 0, 55L, sortedSetOf(refAttribute))

        mockkStatic(TO_EXT_ATTRIBUTE_EXTENSIONS) {
            every { attribute.toExtAttribute() } returns extTestAttribute2
            every { filesSource.getFile(refFile.name) } returns systemFile

            val extRefFile = refFile.toExtFile(filesSource)
            assertThat(extRefFile.fileName).isEqualTo(refFile.name)
            assertThat(extRefFile.file).isEqualTo(systemFile)
            assertThat(extRefFile.attributes).containsOnly(extTestAttribute2)
        }
    }
}
