package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FileDeserializerTest {
    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize no value`() {
        val exception = assertThrows<IllegalStateException> { testInstance.readValue<File>("{}") }

        assertThat(exception.message).isEqualTo("Expecting to find property with 'path' in node '{}'")
    }

    @Test
    fun `deserialize with wrong path type`() {
        val invalidJson = jsonObj {
            "path" to jsonArray(1, 2, 3)
        }.toString()

        val node = "{\"path\":[1,2,3]}"
        val exception = assertThrows<IllegalArgumentException> { testInstance.readValue<File>(invalidJson) }
        assertThat(exception.message).isEqualTo(
            "Expecting node: '$node', property: 'path' to be of type 'TextNode' but 'ArrayNode' was found instead"
        )
    }

    @Test
    fun `deserialize with empty path`() {
        val invalidJson = jsonObj {
            "path" to ""
        }.toString()

        val exception = assertThrows<InvalidElementException> { testInstance.readValue<File>(invalidJson) }
        assertThat(exception.message).isEqualTo("$REQUIRED_FILE_PATH. Element was not created.")
    }

    @Test
    fun `deserialize with attributes`() {
        val fileJson = jsonObj {
            "path" to "/path/file.txt"
            "attributes" to jsonArray({
                "name" to "attr name"
                "value" to "attr value"
            })
        }.toString()

        val file = testInstance.readValue<File>(fileJson)
        val expected = File("/path/file.txt", attributes = listOf(Attribute("attr name", "attr value")))

        assertThat(file).isEqualTo(expected)
    }

    @Test
    fun `deserialize with no attributes`() {
        val fileJson = jsonObj {
            "path" to "/path/file.txt"
        }.toString()

        val file = testInstance.readValue<File>(fileJson)

        assertThat(file).isEqualTo(File("/path/file.txt"))
    }

    @Test
    fun `deserialize with size`() {
        val fileJson = jsonObj {
            "path" to "/path/file.txt"
            "size" to "125"
            "attributes" to jsonArray({
                "name" to "attr name"
                "value" to "attr value"
            })
            "type" to "file"
        }.toString()

        val file = testInstance.readValue<File>(fileJson)
        val expected = File("/path/file.txt", attributes = listOf(Attribute("attr name", "attr value")))

        assertThat(file).isEqualTo(expected)
    }
}
