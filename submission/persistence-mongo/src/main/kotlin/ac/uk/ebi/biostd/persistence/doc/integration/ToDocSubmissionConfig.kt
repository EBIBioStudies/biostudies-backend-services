package ac.uk.ebi.biostd.persistence.doc.integration

import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocFileList
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSection
import ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmission
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToDocSubmissionConfig {
    @Bean
    internal fun toDocSubmission(toDocSection: ToDocSection): ToDocSubmission = ToDocSubmission(toDocSection)

    @Bean
    internal fun toDocSection(toDocFileList: ToDocFileList): ToDocSection = ToDocSection(toDocFileList)

    @Bean
    internal fun toDocFileList(): ToDocFileList = ToDocFileList()
}
