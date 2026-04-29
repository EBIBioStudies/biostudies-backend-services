package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocFileListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocLinkListMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSectionMapper
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToDocSubmissionConfig {
    @Bean
    internal fun toDocSubmission(toDocSectionMapper: ToDocSectionMapper): ToDocSubmissionMapper = ToDocSubmissionMapper(toDocSectionMapper)

    @Bean
    internal fun toDocSection(
        toDocFileListMapper: ToDocFileListMapper,
        toDocLinkListMapper: ToDocLinkListMapper,
    ): ToDocSectionMapper = ToDocSectionMapper(toDocFileListMapper, toDocLinkListMapper)

    @Bean
    internal fun toDocFileList(): ToDocFileListMapper = ToDocFileListMapper()

    @Bean
    internal fun toDocLinkList(): ToDocLinkListMapper = ToDocLinkListMapper()
}
