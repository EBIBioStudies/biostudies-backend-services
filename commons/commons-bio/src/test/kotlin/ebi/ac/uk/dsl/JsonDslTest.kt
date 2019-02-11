package ebi.ac.uk.dsl

import ebi.ac.uk.dsl.json.JsonNull
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonNull
import ebi.ac.uk.dsl.json.jsonObj
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JsonDslTest {

    @Nested
    @DisplayName("Special encoding")
    inner class SpecialTest {

        @Test
        fun `when quotes`() {
            val obj = jsonObj {
                "string" to "string \"value \n"
            }
            assertThat(obj.toString()).isEqualTo("{\"string\": \"string \\\"value \\n\"}")
        }
    }

    @Nested
    @DisplayName("Object test")
    inner class ObjectTest {

        @Test
        fun `simple object`() {
            val obj = jsonObj {
                "string" to "string value"
                "boolean" to true
                "number" to 50
                "null prop" to JsonNull
            }

            assertThat(obj.toString()).isEqualTo(
                "{\"string\": \"string value\", \"boolean\": true, \"number\": 50, \"null prop\": null}")
        }

        @Test
        fun `nested object`() {
            val obj = jsonObj {
                "object" to jsonObj {
                    "name" to "jhon"
                }
            }

            assertThat(obj.toString()).isEqualTo("{\"object\": {\"name\": \"jhon\"}}")
        }

        @Test
        fun `object with array`() {
            val obj = jsonObj {
                "array" to jsonArray(1, 2, 3)
            }

            assertThat(obj.toString()).isEqualTo("{\"array\": [1, 2, 3]}")
        }
    }

    @Nested
    @DisplayName("Arrays test")
    inner class ArrayTest {

        @Test
        fun `string array`() {
            val obj = jsonArray("a", "b", "c")
            assertThat(obj.toString()).isEqualTo("[\"a\", \"b\", \"c\"]")
        }

        @Test
        fun `number array`() {
            val obj = jsonArray(1, 2, 3)
            assertThat(obj.toString()).isEqualTo("[1, 2, 3]")
        }

        @Test
        fun `null array`() {
            val obj = jsonArray(JsonNull, JsonNull, JsonNull)
            assertThat(obj.toString()).isEqualTo("[null, null, null]")
        }

        @Test
        fun `arrays of array`() {
            val obj = jsonArray(
                jsonArray(1, 2, 3),
                jsonArray("a", "b")
            )

            assertThat(obj.toString()).isEqualTo("[[1, 2, 3], [\"a\", \"b\"]]")
        }

        @Test
        fun `objects arrays`() {
            val obj = jsonArray(
                jsonObj {
                    "age" to 1
                }, jsonObj {
                "age" to 2
            })

            assertThat(obj.toString()).isEqualTo("[{\"age\": 1}, {\"age\": 2}]")
        }

        @Test
        fun `mix base type array`() {
            val obj = jsonArray(jsonNull, 1, "c")
            assertThat(obj.toString()).isEqualTo("[null, 1, \"c\"]")
        }
    }
}
