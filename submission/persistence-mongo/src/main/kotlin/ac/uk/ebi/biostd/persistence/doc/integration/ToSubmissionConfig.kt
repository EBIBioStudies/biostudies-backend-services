package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.common.FilesResolver

@Configuration
class ToSubmissionConfig {
    @Bean
    fun toSubmissionMapper(toSectionMapper: ToSectionMapper) = ToSubmissionMapper(toSectionMapper)

    @Bean
    fun toSectionMapper(toFileListMapper: ToFileListMapper) = ToSectionMapper(toFileListMapper)

    @Bean
    fun toFileListMapper(
        serializationService: SerializationService,
        extSerializationService: ExtSerializationService,
        resolver: FilesResolver
    ) = ToFileListMapper(serializationService, extSerializationService, resolver)
}
