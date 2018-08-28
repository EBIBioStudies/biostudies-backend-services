package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.common.LinksTable
import ac.uk.ebi.biostd.submission.Attribute
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TableJsonSerializerTest {

    @Test
    fun `deserialize table of links` () {
        val url = listOf("12345", "6789")
        val attr = Attribute("attr1", "attr1 value", false, listOf())

        val json = """[
            |{
            |  "url": "${url[0]}",
            |  "attributes": [
            |    {
            |       "name": "${attr.name}",
            |       "value": "${attr.value}"
            |    },
            |    {
            |       "name": "attr2",
            |       "value": "attr2 value"
            |    }
            |  ]
            |},
            |{
            |   "url": "${url[1]}"
            |}
            |]""".trimMargin()

        val table = JsonSerializer().deserialize(json, LinksTable::class.java)

        val rows = table.elements
        assertThat(rows).hasSize(2)
        assertThat(rows[0].url).isEqualTo(url[0])
        assertThat(rows[0].attributes).hasSize(2)
        assertThat(rows[0].attributes[0]).isEqualTo(attr)

        assertThat(rows[1].url).isEqualTo(url[1])
        assertThat(rows[1].attributes).isEmpty()
    }
}
