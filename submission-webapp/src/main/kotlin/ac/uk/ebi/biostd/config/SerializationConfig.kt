package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.mapping.AttributesMapper
import ac.uk.ebi.biostd.mapping.SectionMapper
import ac.uk.ebi.biostd.mapping.SubmissionMapper
import ac.uk.ebi.biostd.mapping.TabularMapper
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRepository
import ac.uk.ebi.biostd.serialization.json.JsonSerializer
import ac.uk.ebi.biostd.serialization.tsv.TsvSerializer
import ac.uk.ebi.biostd.serialization.xml.XmlSerializer
import ac.uk.ebi.biostd.service.SubmissionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(PersistenceConfig::class)
class SerializationConfig {

    @Bean
    fun submissionService(
            subRepository: SubmissionRepository,
            modelMapper: SubmissionMapper,
            jsonSerializer: JsonSerializer,
            tsvSerializer: TsvSerializer,
            xmlSerializer: XmlSerializer) =
            SubmissionService(subRepository, modelMapper, jsonSerializer, tsvSerializer, xmlSerializer)

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

    @Configuration
    class MappingConfig {

        @Bean
        fun attributeMapper() = AttributesMapper()

        @Bean
        fun tabularMapper(attributesMapper: AttributesMapper) = TabularMapper(attributesMapper)

        @Bean
        fun sectionMapper(attributesMapper: AttributesMapper, tabularMapper: TabularMapper) =
                SectionMapper(attributesMapper, tabularMapper)

        @Bean
        fun submissionMapper(attributesMapper: AttributesMapper, sectionMapper: SectionMapper) =
                SubmissionMapper(attributesMapper, sectionMapper)
    }
}
