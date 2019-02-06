package ebi.ac.uk.util.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class UrlUtilsTest {

    @ParameterizedTest(name = "when url is {0} result should be {1}")
    @CsvSource(
        "/foo, /foo",
        "//foo, /foo",
        "foo/, /foo",
        "foo/bar, /foo/bar",
        "foo/bar/../baz, /foo/baz",
        "foo//bar, /foo/bar"
    )
    fun getRelPath(url: String, expected: String) {
        assertThat(normalize(url)).isEqualTo(expected)
    }
}
