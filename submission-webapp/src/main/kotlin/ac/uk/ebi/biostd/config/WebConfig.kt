package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.config.SerializationConfig.SerializerConfig
import ac.uk.ebi.biostd.files.web.PathDescriptorResolver
import ac.uk.ebi.biostd.submission.converters.PagetabConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@Import(SerializerConfig::class)
class WebConfig : WebMvcConfigurer {

    @Bean
    fun jsonPagetabConverter(serializationService: SerializationService) = PagetabConverter(serializationService)

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(PathDescriptorResolver())
    }
}
