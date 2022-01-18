package ac.uk.ebi.biostd.json.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.constants.FileFields
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class TableJsonSerializerTest {

    private val testInstance = createSerializer()

    private val attribute = Attribute("name attr", "value attr")
    private val linksTable = LinksTable(
        listOf(Link("a url", listOf(attribute)), Link("a url")))
    private val filesTable = FilesTable(listOf(
        File(path = "file1", size = 11L, type = FileFields.FILE.value, attributes = listOf(attribute)),
        File(path = "file2", size = 12L, type = FileFields.FILE.value)))
    private val sectionTable = SectionsTable(listOf(
        Section(accNo = "SECT-123", type = "Study1", attributes = listOf(attribute)),
        Section(accNo = "SECT-456", type = "Study2")))

    @Test
    fun `serialize LinksTable`() {
        val json = testInstance.writeValueAsString(linksTable)
        val expected = jsonArray(
            {
                "url" to "a url"
                "attributes" to jsonArray(
                    {
                        "name" to "name attr"
                        "value" to "value attr"
                        "reference" to false
                    }
                )
            },
            {
                "url" to "a url"
            }
        )

        JSONAssert.assertEquals("invalid linksTable json", json, expected.toString(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `serialize FilesTable`() {
        val json = testInstance.writeValueAsString(filesTable)
        val expected = jsonArray(
            {
                "path" to "file1"
                "size" to 11L
                "type" to FileFields.FILE.value
                "attributes" to jsonArray(
                    {
                        "name" to "name attr"
                        "value" to "value attr"
                        "reference" to false
                    }
                )
            },
            {
                "path" to "file2"
                "size" to 12L
                "type" to FileFields.FILE.value
            }
        )


        JSONAssert.assertEquals("invalid filesTable json", json, expected.toString(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `serialize SectionsTable`() {
        val json = testInstance.writeValueAsString(sectionTable)
        val expected = jsonArray (
            {
                "accNo" to "SECT-123"
                "type" to "Study1"
                "attributes" to jsonArray(
                    {
                        "name" to "name attr"
                        "value" to "value attr"
                        "reference" to false
                    }
                )
            },
            {
                "accNo" to "SECT-456"
                "type" to "Study2"
            }
        )

        JSONAssert.assertEquals("invalid sectionsTable json", json, expected.toString(), JSONCompareMode.LENIENT)
    }

    companion object {
        fun createSerializer(): ObjectMapper {
            val module = SimpleModule()
            module.addSerializer(Table::class.java, TableJsonSerializer())

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}