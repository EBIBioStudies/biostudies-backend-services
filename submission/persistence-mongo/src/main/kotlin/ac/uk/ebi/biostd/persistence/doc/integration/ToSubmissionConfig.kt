package ac.uk.ebi.biostd.persistence.doc.integration

import ebi.ac.uk.extended.mapping.to.ToFileList
import ebi.ac.uk.extended.mapping.to.ToSection
import ebi.ac.uk.extended.mapping.to.ToSubmission
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToSubmissionConfig {
    @Bean
    fun toSubmission(): ToSubmission = ToSubmission(ToSection(ToFileList()))
}
