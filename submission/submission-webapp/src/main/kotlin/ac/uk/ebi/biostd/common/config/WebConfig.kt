package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.common.config.SerializationConfig.SerializerConfig
import ac.uk.ebi.biostd.files.web.common.GroupPathDescriptorResolver
import ac.uk.ebi.biostd.files.web.common.UserPathDescriptorResolver
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
        argumentResolvers.add(UserPathDescriptorResolver())
        argumentResolvers.add(GroupPathDescriptorResolver())
    }
}
