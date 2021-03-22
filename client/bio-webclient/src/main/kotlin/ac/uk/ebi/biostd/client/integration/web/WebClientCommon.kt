package ac.uk.ebi.biostd.client.integration.web

import ac.uk.ebi.biostd.client.exception.BioWebClientErrorHandler
import ac.uk.ebi.biostd.client.interceptor.HttpHeaderInterceptor
import ac.uk.ebi.biostd.client.interceptor.ServerValidationInterceptor
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.util.collections.replace
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import java.nio.charset.StandardCharsets.UTF_8

internal fun template(baseUrl: String): RestTemplate {
    return RestTemplate().apply {
        errorHandler = BioWebClientErrorHandler()
        messageConverters = getConverters(messageConverters)
        interceptors = interceptors()
        uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
    }
}

internal fun <T> jsonHttpEntityOf(value: T): HttpEntity<T> {
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_JSON
    headers.accept = listOf(MediaType.APPLICATION_JSON)
    return HttpEntity(value, headers)
}

/**
 * Filter out XML and String ISO_8859_1 converters. First one because no XML message is required (Api use json) and
 * second one as UTF8 format is favor.
 */
private fun getConverters(converters: List<HttpMessageConverter<*>>): List<HttpMessageConverter<*>> {
    return converters
        .filterNot { it is MappingJackson2XmlHttpMessageConverter }
        .replace({ it is StringHttpMessageConverter }, StringHttpMessageConverter(UTF_8))
}

/**
 * Declare two interceptors.
 *
 * @see ServerValidationInterceptor which handle errors are provide formatted error message.
 * @see HttpHeaderInterceptor which provide json default accept header.
 */
private fun interceptors(): List<ClientHttpRequestInterceptor> =
    listOf(ServerValidationInterceptor(), HttpHeaderInterceptor(ACCEPT, APPLICATION_JSON))

