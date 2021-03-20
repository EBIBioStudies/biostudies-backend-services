package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FileExtTest {
    @Test
    fun `file type ext`() {
        val file = File("File1.txt")
        file["type"] = "Text"

        assertThat(file["type"]).isEqualTo("Text")
        assertThat(file.attributes).hasSize(1)
        assertThat(file.attributes.first()).isEqualTo(Attribute(FileFields.TYPE, "Text"))
    }

    @Test
    fun extension() {
        val file = File("/a/path/to/File1.txt")
        assertThat(file.extension).isEqualTo("txt")
    }
}
