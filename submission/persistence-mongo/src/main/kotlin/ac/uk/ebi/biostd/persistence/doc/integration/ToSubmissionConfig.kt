package ac.uk.ebi.biostd.persistence.doc.integration

import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class ToSubmissionConfig {
    @Bean
    fun toSubmissionMapper(toSectionMapper: ToSectionMapper): ToSubmissionMapper = ToSubmissionMapper(toSectionMapper)

    @Bean
    fun ToSectionMapper(toFileListMapper: ToFileListMapper): ToSectionMapper = ToSectionMapper(toFileListMapper)

    @Bean
    fun toFileListMapper(serializationService: ExtSerializationService) = ToFileListMapper(serializationService)
}
