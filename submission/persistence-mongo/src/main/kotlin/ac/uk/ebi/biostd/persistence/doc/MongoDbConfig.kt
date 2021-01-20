package ac.uk.ebi.biostd.persistence.doc

import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocAttributeConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileListConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocLinkConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocLinkTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocSectionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocSubmissionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.AttributeConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileListConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.SectionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.SubmissionConverter
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories(basePackageClasses = [
    SubmissionMongoRepository::class,
    DocSubmission::class
])
@ConditionalOnProperty(prefix = "app.persistence", name = ["enabledMongo"])
@EnableConfigurationProperties
class MongoDbConfig(
    @Value("\${spring.data.mongodb.database}") val mongoDatabase: String,
    @Value("\${spring.data.mongodb.uri}") val mongoUri: String
) : AbstractMongoClientConfiguration() {

    override fun getDatabaseName(): String = mongoDatabase

    @Bean
    override fun mongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    @Bean
    override fun customConversions(): MongoCustomConversions {
        val converters = mutableListOf<Converter<*, *>>()
        converters.add(docSubmissionConverter())
        converters.add(submissionConverter())
        return MongoCustomConversions(converters)
    }

    private fun docSubmissionConverter(): DocSubmissionConverter {
        val docAttributeConverter = DocAttributeConverter()
        val docFileConverter = DocFileConverter(docAttributeConverter)
        val docFileListConverter = DocFileListConverter(docFileConverter)
        val docFileTableConverter = DocFileTableConverter(docFileConverter)
        val docLinkConverter = DocLinkConverter(docAttributeConverter)
        val docLinksTableConverter = DocLinkTableConverter(docLinkConverter)
        val docSectionConverter = DocSectionConverter(
            docAttributeConverter,
            docLinkConverter,
            docLinksTableConverter,
            docFileConverter,
            docFileTableConverter,
            docFileListConverter
        )
        return DocSubmissionConverter(docSectionConverter, docAttributeConverter)
    }

    private fun submissionConverter(): SubmissionConverter {
        val attributeConverter = AttributeConverter()
        val fileConverter = FileConverter(attributeConverter)
        val fileListConverter = FileListConverter(fileConverter)
        val fileTableConverter = FileTableConverter(fileConverter)
        val linkConverter = LinkConverter(attributeConverter)
        val linksTableConverter = LinkTableConverter(linkConverter)
        val sectionConverter = SectionConverter(
            attributeConverter,
            linkConverter,
            linksTableConverter,
            fileConverter,
            fileTableConverter,
            fileListConverter
        )
        return SubmissionConverter(sectionConverter, attributeConverter)
    }
}
