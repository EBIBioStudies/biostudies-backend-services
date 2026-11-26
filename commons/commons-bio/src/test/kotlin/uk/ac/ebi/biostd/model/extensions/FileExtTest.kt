package uk.ac.ebi.biostd.model.extensions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.constants.FileFields

class FileExtTest {
    @Test
    fun `file type ext`() {
        val file = BioFile("File1.txt")
        file["type"] = "Text"

        assertThat(file["type"]).isEqualTo("Text")
        assertThat(file.attributes).hasSize(1)
        assertThat(file.attributes.first()).isEqualTo(Attribute(FileFields.TYPE.value, "Text"))
    }

    @Test
    fun extension() {
        val file = BioFile("/a/path/to/File1.txt")
        assertThat(file.extension).isEqualTo("txt")
    }
}
