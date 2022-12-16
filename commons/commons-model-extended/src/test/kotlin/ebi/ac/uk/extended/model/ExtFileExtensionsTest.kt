package ebi.ac.uk.extended.model

import ebi.ac.uk.extended.model.ExtFileType.FILE
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(MockKExtension::class)
class ExtFileExtensionsTest {
    @Test
    fun `copy fire file`() {
        val fireFile = FireFile(
            "fire-id",
            "fire-path",
            true,
            "file-path",
            "rel-path",
            "md5",
            1L,
            FILE,
            listOf(ExtAttribute("Attribute", "Old"))
        )
        val newAttributes = listOf(ExtAttribute("Override", "New"))
        val copied = fireFile.copyWithAttributes(newAttributes)

        assertThat(copied).isEqualToIgnoringGivenFields(fireFile, "attributes")
        assertThat(copied.attributes).isEqualTo(newAttributes)
    }

    @Test
    fun `copy nfs file`(
        @MockK file: File
    ) {
        val nfsFile = NfsFile(
            "file-path",
            "rel-path",
            file,
            "full-path",
            "md5",
            1L,
            listOf(ExtAttribute("Attribute", "Old")),
            FILE
        )
        val newAttributes = listOf(ExtAttribute("Override", "New"))
        val copied = nfsFile.copyWithAttributes(newAttributes)

        assertThat(copied).isEqualToIgnoringGivenFields(nfsFile, "attributes")
        assertThat(copied.attributes).isEqualTo(newAttributes)
    }
}
