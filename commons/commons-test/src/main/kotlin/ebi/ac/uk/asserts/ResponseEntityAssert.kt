package ebi.ac.uk.asserts

import ebi.ac.uk.api.ClientResponse
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpStatus

fun <T> assertThat(response: ClientResponse<T>): ResponseEntityAssert<T> = ResponseEntityAssert(response)

class ResponseEntityAssert<T>(actual: ClientResponse<T>) :
    AbstractAssert<ResponseEntityAssert<T>, ClientResponse<*>>(actual, ResponseEntityAssert::class.java) {
    fun isSuccessful() {
        assertThat(actual).isNotNull
        assertThat(actual.statusCode).isEqualTo(HttpStatus.OK.value())
        assertThat(actual.body).isNotNull
    }
}
