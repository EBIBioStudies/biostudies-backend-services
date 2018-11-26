package ac.uk.ebi.biostd.json

import ebi.ac.uk.dsl.jsonArray
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.SectionsTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TableJsonSerializerTest {

    @Test
    fun `deserialize table of links`() {
        val url = listOf("12345", "6789")
        val attr = Attribute("attr1", "attr1 value")

        val json = jsonArray({
            "url" to url[0]
            "attributes" to jsonArray({
                "name" to attr.name
                "value" to attr.value
            }, {
                "name" to "attr2"
                "value" to "attr2 value"
            })
        }, {
            "url" to url[1]
        }).toString()

        val table = JsonSerializer.mapper.readValue(json, LinksTable::class.java)

        val rows = table.elements
        assertThat(rows).hasSize(2)
        assertThat(rows[0].url).isEqualTo(url[0])
        assertThat(rows[0].attributes).hasSize(2)
        assertThat(rows[0].attributes[0]).isEqualTo(attr)

        assertThat(rows[1].url).isEqualTo(url[1])
        assertThat(rows[1].attributes).isEmpty()
    }

    @Test
    fun `deserialize table of sections`() {
        val ids = listOf("12345", "6789")
        val attr = Attribute("attr1", "attr1 value")

        val json = jsonArray({
            "accNo" to ids[0]
            "attributes" to jsonArray({
                "name" to "Run"
                "value" to "Type"
            }, {
                "name" to attr.name
                "value" to attr.value
            }, {
                "name" to "attr2"
                "value" to "attr2 value"
            })
        }, {
            "accNo" to ids[1]
            "attributes" to jsonArray({
                "name" to "Run"
                "value" to "Type"
            })
        }).toString()

        val table = JsonSerializer.mapper.readValue(json, SectionsTable::class.java)

        val rows = table.elements
        assertThat(rows).hasSize(2)
        assertThat(rows[0].accNo).isEqualTo(ids[0])
        assertThat(rows[0].attributes).hasSize(3)
    }

    @Test
    fun `serialize table of links`() {
        val attr = Attribute(
            name = "attrName",
            value = "attrValue",
            reference = false,
            valueAttrs = mutableListOf(AttributeDetail("n", "v")))
        val link = Link("1234", mutableListOf(attr))
        val table = LinksTable(listOf(link))

        val table2 = JsonSerializer.mapper.readValue(JsonSerializer.mapper.writeValueAsString(table), LinksTable::class.java)

        assertThat(table).isEqualTo(table2)
    }
}
