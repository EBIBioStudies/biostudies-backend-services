package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRepository
import ac.uk.ebi.biostd.service.SubmissionService
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(PersistenceConfig::class)
class SerializationConfig {

    @Bean
    fun submissionService(subRepository: SubmissionRepository,
                          jsonSerializer: JsonSerializer,
                          tsvSerializer: TsvSerializer,
                          xmlSerializer: XmlSerializer,
                          submissionSubmitter: SubmissionSubmitter) =

            SubmissionService(submissionSubmitter, subRepository, jsonSerializer, tsvSerializer, xmlSerializer)

    @Configuration
    class SerializerConfig {

        @Bean
        fun jsonSerializer(): JsonSerializer {
            return JsonSerializer()
        }

        @Bean
        fun xmlSerializer(): XmlSerializer {
            return XmlSerializer()
        }

        @Bean
        fun tsvSerializer(): TsvSerializer {
            return TsvSerializer()
        }
    }
}
