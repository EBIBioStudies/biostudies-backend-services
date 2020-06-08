package uk.ac.ebi.extended.serialization

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.ac.ebi.extended.serialization.constants.ExtType
import uk.ac.ebi.extended.serialization.exception.InvalidExtTypeException

class ExtTypeTest {
    @Test
    fun `invalid ext type`() {
        val exception = assertThrows<InvalidExtTypeException> { ExtType.valueOf("any") }
        assertThat(exception.message).isEqualTo("The given ext type 'any' is invalid")
    }
}
