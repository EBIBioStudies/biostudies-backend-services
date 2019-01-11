package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import ebi.ac.uk.dsl.jsonArray
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val ATTR_NAME = "attr1"
private const val ATTR_VAL = "attr1 value"
private const val NAME_DETAIL_NAME = "name detail name"
private const val NAME_DETAIL_VALUE = "name detail value"
private const val VALUE_DETAIL_NAME = "value detail name"
private const val VALUE_DETAIL_VALUE = "value detail value"

class TableJsonDeserializerTest {

    private val testAttribute = createTestAttribute()

    @Test
    fun `deserialize table of links`() {
        val expectedTable = LinksTable(mutableListOf(
            Link(url = "link url", attributes = mutableListOf(testAttribute)),
            Link(url = "another url")))

        val json = jsonArray({
            "url" to "link url"
            "attributes" to jsonArray({
                "name" to ATTR_NAME
                "value" to ATTR_VAL
                "valqual" to jsonArray({
                    "name" to VALUE_DETAIL_NAME
                    "value" to VALUE_DETAIL_VALUE
                })
                "namequal" to jsonArray({
                    "name" to NAME_DETAIL_NAME
                    "value" to NAME_DETAIL_VALUE
                })
            })
        }, {
            "url" to "another url"
        }).toString()

        val table = JsonSerializer.mapper.readValue(json, LinksTable::class.java)
        assertThat(table.rows).isEqualTo(expectedTable.rows)
    }

    @Test
    fun `deserialize table of sections`() {
        val expectedTable = SectionsTable(mutableListOf(
            Section(accNo = "abc123", type = "Study", attributes = mutableListOf(testAttribute)),
            Section(accNo = "xyz123", type = "Study")))

        val json = jsonArray({
            "accNo" to "abc123"
            "type" to "Study"
            "attributes" to jsonArray({
                "name" to ATTR_NAME
                "value" to ATTR_VAL
                "valqual" to jsonArray({
                    "name" to VALUE_DETAIL_NAME
                    "value" to VALUE_DETAIL_VALUE
                })
                "namequal" to jsonArray({
                    "name" to NAME_DETAIL_NAME
                    "value" to NAME_DETAIL_VALUE
                })
            })
        }, {
            "accNo" to "xyz123"
            "type" to "Study"
        }).toString()

        val table = JsonSerializer.mapper.readValue(json, SectionsTable::class.java)
        assertThat(table.rows).isEqualTo(expectedTable.rows)
    }

    @Test
    fun `deserialize table of files`() {
        val expectedTable = FilesTable(mutableListOf(
            File(path = "file1", attributes = mutableListOf(testAttribute)),
            File(path = "file2")))

        val json = jsonArray({
            "path" to "file1"
            "attributes" to jsonArray({
                "name" to ATTR_NAME
                "value" to ATTR_VAL
                "valqual" to jsonArray({
                    "name" to VALUE_DETAIL_NAME
                    "value" to VALUE_DETAIL_VALUE
                })
                "namequal" to jsonArray({
                    "name" to NAME_DETAIL_NAME
                    "value" to NAME_DETAIL_VALUE
                })
            })
        }, {
            "path" to "file2"
        }).toString()

        val table = JsonSerializer.mapper.readValue(json, FilesTable::class.java)
        assertThat(table.rows).isEqualTo(expectedTable.rows)
    }

    private fun createTestAttribute() = Attribute(ATTR_NAME, ATTR_VAL,
        nameAttrs = mutableListOf(AttributeDetail(NAME_DETAIL_NAME, NAME_DETAIL_VALUE)),
        valueAttrs = mutableListOf(AttributeDetail(VALUE_DETAIL_NAME, VALUE_DETAIL_VALUE)))
}
