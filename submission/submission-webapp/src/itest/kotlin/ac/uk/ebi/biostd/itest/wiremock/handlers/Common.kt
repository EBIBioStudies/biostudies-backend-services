package ac.uk.ebi.biostd.itest.wiremock.handlers

import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import org.springframework.http.HttpStatus

internal const val FIRE_BASE_URL = "/fire/v\\d*.\\d*/objects"

interface RequestHandler {
    val method: RequestMethod
    val urlPattern: Regex

    fun handle(rqt: Request): ResponseDefinition

    fun handleSafely(rqt: Request): ResponseDefinition = runCatching { handle(rqt) }.getOrElse { asResponse(it) }

    private fun asResponse(exception: Throwable): ResponseDefinition =
        when (exception) {
            is FireException -> ResponseDefinition(exception.status.value(), exception.message)
            else -> ResponseDefinition(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.message)
        }
}

class FireException(message: String, val status: HttpStatus) : Exception(message)
