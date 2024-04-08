package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@Configuration
class ToDocSubmissionConfig {
    @Bean
    internal fun toDocSubmission(toDocSectionMapper: ToDocSectionMapper): ToDocSubmissionMapper = ToDocSubmissionMapper(toDocSectionMapper)

    @Bean
    internal fun toDocSection(toDocFileListMapper: ToDocFileListMapper): ToDocSectionMapper = ToDocSectionMapper(toDocFileListMapper)

    @Bean
    internal fun toDocFileList(serializationService: ExtSerializationService): ToDocFileListMapper =
        ToDocFileListMapper(serializationService)
}
