package ebi.ac.uk.security.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

internal class TokenPayloadTest {

    @Test
    fun `test constructor`() {
        val userId = 55L
        val email = "auser@email.com"
        val fullName = "Jhon doe"

        val token = TokenPayload(userId, email, fullName)
        assertThat(token.id).isEqualTo(userId)
        assertThat(token.email).isEqualTo(email)
        assertThat(token.fullName).isEqualTo(fullName)
        assertThat(token.creationTime).isCloseTo(OffsetDateTime.now().toEpochSecond(), Offset.offset(3000L))
    }
}
