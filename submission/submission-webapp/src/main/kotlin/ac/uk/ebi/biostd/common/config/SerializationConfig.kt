package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.persistence.service.SubmissionRepository
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.service.SubmissionService
import ac.uk.ebi.biostd.tsv.serialization.TsvToStringSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(PersistenceConfig::class)
class SerializationConfig {

    @Bean
    fun submissionService(
        subRepository: SubmissionRepository,
        jsonSerializer: JsonSerializer,
        tsvSerializer: TsvToStringSerializer,
        xmlSerializer: XmlSerializer,
        persistenceContext: PersistenceContext,
        submissionSubmitter: SubmissionSubmitter
    ) =
        SubmissionService(subRepository, persistenceContext, jsonSerializer, tsvSerializer, xmlSerializer, submissionSubmitter)

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
        fun tsvSerializer(): TsvToStringSerializer {
            return TsvToStringSerializer()
        }
    }
}
