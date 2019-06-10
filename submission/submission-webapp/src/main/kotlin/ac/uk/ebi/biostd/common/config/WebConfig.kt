package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.files.web.common.GroupPathDescriptorResolver
import ac.uk.ebi.biostd.files.web.common.UserPathDescriptorResolver
import ac.uk.ebi.biostd.integration.ISerializationService
import ac.uk.ebi.biostd.submission.converters.PagetabConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    @Bean
    fun jsonPagetabConverter(serializationService: ISerializationService) = PagetabConverter(serializationService)

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(UserPathDescriptorResolver())
        argumentResolvers.add(GroupPathDescriptorResolver())
    }
}
