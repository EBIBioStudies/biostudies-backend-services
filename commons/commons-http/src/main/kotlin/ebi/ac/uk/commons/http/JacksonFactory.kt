package ebi.ac.uk.commons.http

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

class JacksonFactory {

    companion object {

        fun createMapper(): ObjectMapper {
            val mapper = ObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            mapper.registerModule(KotlinModule())
            return mapper
        }

        fun jsonRestTemplate(): RestTemplate {
            val template = RestTemplate()
            template.messageConverters = listOf(MappingJackson2HttpMessageConverter())
            return template
        }
    }
}
