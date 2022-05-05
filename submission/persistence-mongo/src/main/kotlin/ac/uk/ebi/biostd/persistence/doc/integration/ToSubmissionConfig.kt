package ac.uk.ebi.biostd.persistence.doc.integration

import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToFilesTableMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class ToSubmissionConfig {
    @Bean
    fun toFilesTableMapper(toFileListMapper: ToFileListMapper) = ToFilesTableMapper(toFileListMapper)

    @Bean
    fun toSubmissionMapper(toSectionMapper: ToSectionMapper) = ToSubmissionMapper(toSectionMapper)

    @Bean
    fun toSectionMapper(toFileListMapper: ToFileListMapper) = ToSectionMapper(toFileListMapper)

    @Bean
    fun toFileListMapper(serializationService: ExtSerializationService) = ToFileListMapper(serializationService)
}
