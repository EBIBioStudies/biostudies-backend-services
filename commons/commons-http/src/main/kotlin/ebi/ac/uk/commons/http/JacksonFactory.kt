package ebi.ac.uk.commons.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset

object JacksonFactory {
    private val stringConverter = StringHttpMessageConverter(Charset.forName("UTF-8"))

    fun createMapper(): ObjectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        registerKotlinModule()
    }

    fun jsonRestTemplate(): RestTemplate =
        RestTemplate().apply {
            messageConverters = listOf(MappingJackson2HttpMessageConverter(), stringConverter)
        }
}
