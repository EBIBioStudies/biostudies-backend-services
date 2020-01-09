package ebi.ac.uk.commons.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

object JacksonFactory {
    fun createMapper(): ObjectMapper = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        registerModule(KotlinModule())
    }

    fun jsonRestTemplate(): RestTemplate =
        RestTemplate().apply {
            messageConverters = listOf(MappingJackson2HttpMessageConverter())
        }
}
