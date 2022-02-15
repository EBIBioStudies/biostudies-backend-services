package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Link
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LinkDeserializerTest {
    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize no value`() {
        val exception = assertThrows<IllegalStateException> { testInstance.readValue<Link>("{}") }

        assertThat(exception.message).isEqualTo("Expecting to find property with 'url' in node '{}'")
    }

    @Test
    fun `deserialize with wrong url type`() {
        val invalidJson = jsonObj {
            "url" to jsonArray(1, 2, 3)
        }.toString()

        val node = "{\"url\":[1,2,3]}"
        val exception = assertThrows<IllegalArgumentException> { testInstance.readValue<Link>(invalidJson) }
        assertThat(exception.message).isEqualTo(
            "Expecting node: '$node', property: 'url' to be of type 'TextNode' but 'ArrayNode' was found instead"
        )
    }

    @Test
    fun `deserialize with attributes`() {
        val linkJson = jsonObj {
            "url" to "a url"
            "attributes" to jsonArray({
                "name" to "attr name"
                "value" to "attr value"
            })
        }.toString()

        val link = testInstance.readValue<Link>(linkJson)
        val expected = Link("a url", attributes = listOf(Attribute("attr name", "attr value")))

        assertThat(link).isEqualTo(expected)
    }

    @Test
    fun `deserialize with no attributes`() {
        val linkJson = jsonObj {
            "url" to "a url"
        }.toString()

        val link = testInstance.readValue<Link>(linkJson)

        assertThat(link).isEqualTo(Link("a url"))
    }
}
