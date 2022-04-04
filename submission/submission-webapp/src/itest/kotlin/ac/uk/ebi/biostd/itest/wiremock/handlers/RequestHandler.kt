package ac.uk.ebi.biostd.itest.wiremock.handlers

import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition

interface RequestHandler {
    val requestMethod: RequestMethod
    val urlPattern: Regex

    fun handle(rqt: Request): ResponseDefinition
}

