package ebi.ac.uk.asserts

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun <T> assertThat(response: ResponseEntity<T>): ResponseEntityAssert<T> = ResponseEntityAssert(response)

class ResponseEntityAssert<T>(actual: ResponseEntity<T>) :
    AbstractAssert<ResponseEntityAssert<T>, ResponseEntity<*>>(actual, ResponseEntityAssert::class.java) {
    fun isSuccessful() {
        assertThat(actual).isNotNull
        assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(actual.body).isNotNull
    }
}
