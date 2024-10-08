package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.files.web.common.FileListPathDescriptorResolver
import ac.uk.ebi.biostd.files.web.common.GroupPathDescriptorResolver
import ac.uk.ebi.biostd.files.web.common.UserPathDescriptorResolver
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ac.uk.ebi.biostd.submission.converters.BioUserResolver
import ac.uk.ebi.biostd.submission.converters.ExtFileTableConverter
import ac.uk.ebi.biostd.submission.converters.ExtPageSubmissionConverter
import ac.uk.ebi.biostd.submission.converters.ExtSubmissionConverter
import ac.uk.ebi.biostd.submission.converters.JsonPagetabConverter
import ac.uk.ebi.biostd.submission.converters.OnBehalfUserRequestResolver
import ac.uk.ebi.biostd.submission.converters.SubmitAttributeConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
@Suppress("MagicNumber")
@EnableGlobalMethodSecurity(prePostEnabled = true)
internal class WebConfig(
    private val serializationService: SerializationService,
    private val extSerializationService: ExtSerializationService,
) : WebMvcConfigurer {
    @Bean
    fun submitterResolver() = BioUserResolver(principalResolver())

    @Bean
    fun principalResolver() = AuthenticationPrincipalArgumentResolver()

    @Bean
    fun tempFileGenerator(properties: ApplicationProperties): TempFileGenerator = TempFileGenerator(properties.persistence.tempDirPath)

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
    }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(0, JsonPagetabConverter(serializationService))
        converters.add(1, ExtSubmissionConverter(extSerializationService))
        converters.add(2, ExtPageSubmissionConverter(extSerializationService))
        converters.add(3, ExtFileTableConverter(extSerializationService))
    }

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(UserPathDescriptorResolver())
        argumentResolvers.add(GroupPathDescriptorResolver())
        argumentResolvers.add(submitterResolver())
        argumentResolvers.add(OnBehalfUserRequestResolver())
        argumentResolvers.add(FileListPathDescriptorResolver())
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(SubmitAttributeConverter())
    }
}
