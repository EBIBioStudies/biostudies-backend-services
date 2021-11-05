package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.configurationprocessor.json.JSONException

class ExtAttributeDetailConverterTest {
    private val testInstance = ExtAttributeDetailConverter()

    @Test
    fun convert() {
        val attribute = jsonObj {
            "name" to "Overridden"
            "value" to "New Value"
        }.toString()

        assertThat(testInstance.convert(attribute)).isEqualTo(ExtAttributeDetail("Overridden", "New Value"))
    }

    @Test
    fun `convert invalid`() {
        val attribute = jsonObj { "name" to "Overridden" }.toString()
        assertThrows<JSONException> { testInstance.convert(attribute) }
    }
}
