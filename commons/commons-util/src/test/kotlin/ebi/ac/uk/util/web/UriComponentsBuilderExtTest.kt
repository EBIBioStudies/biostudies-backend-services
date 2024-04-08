package ebi.ac.uk.util.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class UriComponentsBuilderExtTest {
    @Test
    fun `optional query parameter`() {
        val url =
            UriComponentsBuilder
                .fromUriString("http://localhost:8080")
                .optionalQueryParam("optional", 12)
                .build()
                .toUriString()

        assertThat(url).isEqualTo("http://localhost:8080?optional=12")
    }

    @Test
    fun `null value optional query parameter`() {
        val url =
            UriComponentsBuilder
                .fromUriString("http://localhost:8080")
                .optionalQueryParam("optional", null)
                .build()
                .toUriString()

        assertThat(url).isEqualTo("http://localhost:8080")
    }
}
