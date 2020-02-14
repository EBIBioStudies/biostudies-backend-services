package ac.uk.ebi.biostd.persistence.converters

import ebi.ac.uk.functions.secondsToInstant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

class NullableEpochDateConverterTest {
    private val testInstance = NullableEpochDateConverter()

    @Test
    fun `convert to entity`() {
        val expected = secondsToInstant(1).atOffset(ZoneOffset.UTC)
        assertThat(testInstance.convertToEntityAttribute(-1)).isNull()
        assertThat(testInstance.convertToEntityAttribute(1)).isEqualTo(expected)
    }

    @Test
    fun `convert to database column`() {
        val testTime = secondsToInstant(1).atOffset(ZoneOffset.UTC)
        assertThat(testInstance.convertToDatabaseColumn(testTime)).isEqualTo(1)
    }
}
