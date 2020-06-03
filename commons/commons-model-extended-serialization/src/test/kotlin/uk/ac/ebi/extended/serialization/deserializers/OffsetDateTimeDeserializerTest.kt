package uk.ac.ebi.extended.serialization.deserializers

import ebi.ac.uk.dsl.json.jsonObj
import org.junit.jupiter.api.Test
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.OffsetDateTime
import java.time.ZoneOffset

class OffsetDateTimeDeserializerTest {
    private val testInstance = ExtSerializationService.mapper

    // TODO test this at submission level and remove this class
    @Test
    fun deserialize() {
        val json = jsonObj { "releaseTime" to "2019-09-21T10:15:24.000000003Z" }
        val date = OffsetDateTime.of(2019, 9, 21, 10, 15, 24, 3, ZoneOffset.UTC)
        val jsonTime = "2019-09-21T10:15:24.000000003Z"
    }
}
