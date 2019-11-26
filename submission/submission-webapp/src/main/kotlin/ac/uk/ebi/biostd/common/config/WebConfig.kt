package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.files.web.common.GroupPathDescriptorResolver
import ac.uk.ebi.biostd.files.web.common.UserPathDescriptorResolver
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.converters.BioUserResolver
import ac.uk.ebi.biostd.submission.converters.PagetabConverter
import ebi.ac.uk.security.integration.components.ISecurityService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val securityService: ISecurityService
) : WebMvcConfigurer {

    @Bean
    fun jsonPagetabConverter(serializationService: SerializationService) = PagetabConverter(serializationService)

    @Bean
    fun submitterConverter() = BioUserResolver(principalResolver(), securityService)

    @Bean
    fun principalResolver() = AuthenticationPrincipalArgumentResolver()

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(UserPathDescriptorResolver())
        argumentResolvers.add(GroupPathDescriptorResolver())
        argumentResolvers.add(submitterConverter())
    }
}
