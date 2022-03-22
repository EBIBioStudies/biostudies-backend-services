package ac.uk.ebi.biostd.persistence.doc.integration

import ebi.ac.uk.extended.mapping.to.ToFileListMapper
import ebi.ac.uk.extended.mapping.to.ToSectionMapper
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToSubmissionConfig {
    @Bean
    fun toSubmission(): ToSubmissionMapper = ToSubmissionMapper(ToSectionMapper(ToFileListMapper()))
}
