package ebi.ac.uk.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonObjTest {

    @Test
    fun `validate json object`() {
        val json = jsonObj {
            "string_prop" to "b"
            "number_prop" to 2
            "object_array" to jsonArray({
                "a" to "b"
            })
            "number_array" to jsonArray(1, 2, 3)
            "string_array" to jsonArray("a", "b")
        }.toString()

        assertThat(json).isEqualToIgnoringWhitespace("""
            {
               "string_prop":"b",
               "number_prop":2,
               "object_array":[
                  {
                     "a":"b"
                  }
               ],
               "number_array":[
                  1,
                  2,
                  3
               ],
               "string_array":[
                  "a",
                  "b"
               ]
            }
        """.trimIndent())
    }

    @Test
    fun `validate json array`() {
        val jsonMatrix = jsonArray(
            jsonArray(1, 2),
            jsonArray(1, 3)).toString()
        assertThat(jsonMatrix).isEqualToIgnoringWhitespace("[[1, 2], [1, 3]]")
    }
}