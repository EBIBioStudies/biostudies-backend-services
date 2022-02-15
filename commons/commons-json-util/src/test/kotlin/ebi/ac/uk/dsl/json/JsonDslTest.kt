package ebi.ac.uk.dsl.json

import ebi.ac.uk.dsl.json.JsonDslTest.Companion.TestEnum.TEST
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

        @Test
        fun `escape all special characters`() {
            val obj = jsonObj {
                "string" to "\n , \t , \b , \r , \' , \\ "
            }
            assertThat(obj.toString()).isEqualTo("{\"string\": \"\\n , \\t , \\b , \\r , \\' , \\\\ \"}")
        }
    }

    @Nested
    @DisplayName("Object test")
    inner class ObjectTest {

        @Test
        fun `simple object`() {
            val obj = jsonObj {
                "string" to "string value"
                "boolean1" to true
                "boolean2" to false
                "number" to 50
                "null prop" to JsonNull
                "enum" to TEST
                "curlyBrackets" to {}
            }
            val expected = """
                    {
                        "string": "string value",
                        "boolean1": true, 
                        "boolean2": false,
                        "number": 50, 
                        "null prop": null,
                        "enum": "TEST",
                        "curlyBrackets": {}
                    }
                """

            ebi.ac.uk.asserts.StringAssertion.assertThat(obj.toString()).isEqualsIgnoringSpacesAndLineBreaks(expected)
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
        fun `null object`() {
            val obj = jsonObj {
                "object" to null
            }

            assertThat(obj.toString()).isEqualTo("{\"object\": null}")
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
        fun `empty array`() {
            val obj = jsonArray()
            assertThat(obj.toString()).isEqualTo("[]")
        }

        @Test
        fun `curly brackets array`() {
            val obj = jsonArray({})
            assertThat(obj.toString()).isEqualTo("[{}]")
        }

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
        fun `enum array`() {
            val obj = jsonArray(TEST)
            assertThat(obj.toString()).isEqualTo("[\"TEST\"]")
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
                },
                jsonObj {
                    "age" to 2
                }
            )

            assertThat(obj.toString()).isEqualTo("[{\"age\": 1}, {\"age\": 2}]")
        }

        @Test
        fun `mix base type array`() {
            val obj = jsonArray(jsonNull, 1, "c")
            assertThat(obj.toString()).isEqualTo("[null, 1, \"c\"]")
        }
    }

    companion object {
        enum class TestEnum { TEST }
    }
}
