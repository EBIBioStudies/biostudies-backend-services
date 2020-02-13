package ac.uk.ebi.biostd.persistence.converters

import ebi.ac.uk.functions.secondsToInstant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneOffset

class EpochDateConverterTest {
    private val converter = EpochDateConverter()
    private val nullableConverter = NullableEpochDateConverter()

    @Test
    fun `convert to entity`() {
        val expected = secondsToInstant(1).atOffset(ZoneOffset.UTC)
        assertThat(converter.convertToEntityAttribute(1)).isEqualTo(expected)
        assertThat(nullableConverter.convertToEntityAttribute(-1)).isNull()
        assertThat(nullableConverter.convertToEntityAttribute(1)).isEqualTo(expected)
    }

    @Test
    fun `convert to database column`() {
        val testTime = secondsToInstant(1).atOffset(ZoneOffset.UTC)
        assertThat(converter.convertToDatabaseColumn(testTime)).isEqualTo(1)
        assertThat(nullableConverter.convertToDatabaseColumn(testTime)).isEqualTo(1)
    }
}
