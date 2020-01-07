package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.files.web.common.GroupPathDescriptorResolver
import ac.uk.ebi.biostd.files.web.common.UserPathDescriptorResolver
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.converters.BioUserResolver
import ac.uk.ebi.biostd.submission.converters.JsonPagetabConverter
import ebi.ac.uk.security.integration.components.ISecurityService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
internal class WebConfig(
    private val securityService: ISecurityService,
    private val serializationService: SerializationService
) : WebMvcConfigurer {

    @Bean
    fun submitterResolver() = BioUserResolver(principalResolver(), securityService)

    @Bean
    fun principalResolver() = AuthenticationPrincipalArgumentResolver()

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
    }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(0, JsonPagetabConverter(serializationService))
    }

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(UserPathDescriptorResolver())
        argumentResolvers.add(GroupPathDescriptorResolver())
        argumentResolvers.add(submitterResolver())
    }
}
