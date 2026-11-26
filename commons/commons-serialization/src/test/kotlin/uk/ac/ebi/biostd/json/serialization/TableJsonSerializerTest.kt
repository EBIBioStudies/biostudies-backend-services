package uk.ac.ebi.biostd.json.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import uk.ac.ebi.biostd.dsl.json.jsonArray
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.FilesTable
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.LinksTable
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.SectionsTable
import uk.ac.ebi.biostd.model.Table
import uk.ac.ebi.biostd.model.constants.FileFields

class TableJsonSerializerTest {
    private val testInstance = createSerializer()

    private val attribute = Attribute("name attr", "value attr")
    private val linksTable =
        LinksTable(
            listOf(Link("a url", listOf(attribute)), Link("a url")),
        )
    private val filesTable =
        FilesTable(
            listOf(
                BioFile(path = "file1", size = 11L, type = FileFields.FILE.value, attributes = listOf(attribute)),
                BioFile(path = "file2", size = 12L, type = FileFields.FILE.value),
            ),
        )
    private val sectionTable =
        SectionsTable(
            listOf(
                Section(accNo = "SECT-123", type = "Study1", attributes = listOf(attribute)),
                Section(accNo = "SECT-456", type = "Study2"),
            ),
        )

    @Test
    fun `serialize LinksTable`() {
        val json = testInstance.writeValueAsString(linksTable)
        val expected =
            jsonArray(
                {
                    "url" to "a url"
                    "attributes" to
                        jsonArray(
                            {
                                "name" to "name attr"
                                "value" to "value attr"
                                "reference" to false
                            },
                        )
                },
                {
                    "url" to "a url"
                },
            )

        JSONAssert.assertEquals("invalid linksTable json", json, expected.toString(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `serialize FilesTable`() {
        val json = testInstance.writeValueAsString(filesTable)
        val expected =
            jsonArray(
                {
                    "path" to "file1"
                    "size" to 11L
                    "type" to FileFields.FILE.value
                    "attributes" to
                        jsonArray(
                            {
                                "name" to "name attr"
                                "value" to "value attr"
                                "reference" to false
                            },
                        )
                },
                {
                    "path" to "file2"
                    "size" to 12L
                    "type" to FileFields.FILE.value
                },
            )

        JSONAssert.assertEquals("invalid filesTable json", json, expected.toString(), JSONCompareMode.LENIENT)
    }

    @Test
    fun `serialize SectionsTable`() {
        val json = testInstance.writeValueAsString(sectionTable)
        val expected =
            jsonArray(
                {
                    "accNo" to "SECT-123"
                    "type" to "Study1"
                    "attributes" to
                        jsonArray(
                            {
                                "name" to "name attr"
                                "value" to "value attr"
                                "reference" to false
                            },
                        )
                },
                {
                    "accNo" to "SECT-456"
                    "type" to "Study2"
                },
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
