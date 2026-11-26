package ac.uk.ebi.biostd.persistence.doc.integration

import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToLinkListMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import uk.ac.ebi.biostd.integration.SerializationService
import uk.ac.ebi.biostd.serialization.common.FilesResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
@Import(value = [SerializationConfiguration::class])
class ToSubmissionConfig {
    @Bean
    fun toFileListMapper(
        serializationService: SerializationService,
        extSerializationService: ExtSerializationService,
        resolver: FilesResolver,
    ) = ToFileListMapper(serializationService, extSerializationService, resolver)

    @Bean
    fun toLinkListMapper(
        serializationService: SerializationService,
        extSerializationService: ExtSerializationService,
        resolver: FilesResolver,
    ) = ToLinkListMapper(serializationService, extSerializationService, resolver)

    @Bean
    fun toSectionMapper(
        toFileListMapper: ToFileListMapper,
        toLinkListMapper: ToLinkListMapper,
    ): ToSectionMapper = ToSectionMapper(toFileListMapper, toLinkListMapper)

    @Bean
    fun toSubmissionMapper(toSectionMapper: ToSectionMapper): ToSubmissionMapper = ToSubmissionMapper(toSectionMapper)
}
