package ac.uk.ebi.biostd.persistence.doc

import ac.uk.ebi.biostd.persistence.doc.db.converters.createFromDocConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.createToDocConverter
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.integration.AppProperties
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoQueryService
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackageClasses = [SubmissionMongoRepository::class, DocSubmission::class])
@EnableConfigurationProperties
class MongoConfig : AbstractMongoClientConfiguration() {

    override fun getDatabaseName(): String = "test"

    override fun mongoClient(): MongoClient = MongoClients.create(appProperties().connection)

    override fun configureConverters(adapter: MongoConverterConfigurationAdapter) {
        val converter = defaultMongoConverter(mongoDbFactory())
        adapter.registerConverter(createToDocConverter<DocLink, DocLinkTable>(converter))
        adapter.registerConverter(createFromDocConverter<DocLink, DocLinkTable>(converter))
        adapter.registerConverter(createToDocConverter<DocFile, DocFileTable>(converter))
        adapter.registerConverter(createFromDocConverter<DocFile, DocFileTable>(converter))
        adapter.registerConverter(createToDocConverter<DocSection, DocSectionTable>(converter))
        adapter.registerConverter(createFromDocConverter<DocSection, DocSectionTable>(converter))
    }

    private fun defaultMongoConverter(factory: MongoDatabaseFactory): MongoConverter =
        MappingMongoConverter(DefaultDbRefResolver(factory), MongoMappingContext())

    @Bean
    fun submissionMongoQueryService(submissionMongoRepository: SubmissionMongoRepository) =
        SubmissionMongoQueryService(submissionMongoRepository)

    @Bean
    fun appProperties() = AppProperties()

    @Bean
    fun submissionDataRepository(submissionMongoRepository: SubmissionMongoRepository, mongoTemplate: MongoTemplate): SubmissionDocDataRepository {
        return SubmissionDocDataRepository(submissionMongoRepository, mongoTemplate)
    }
}
