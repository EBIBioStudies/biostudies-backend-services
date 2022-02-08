package ac.uk.ebi.biostd.persistence.doc

import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocAttributeConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileListConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileListDocFileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocFileTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocLinkConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocLinkTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocSectionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.from.DocSubmissionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.AttributeConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileListConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileListDocFileConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.FileTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.LinkTableConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.SectionConverter
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.SubmissionConverter
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.migrations.ChangeSet001
import ac.uk.ebi.biostd.persistence.doc.migrations.ChangeSet002
import ac.uk.ebi.biostd.persistence.doc.migrations.ChangeSet003
import ac.uk.ebi.biostd.persistence.doc.migrations.ChangeSet004
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongoV3Driver
import com.github.cloudyrock.spring.v5.MongockSpring5
import com.github.cloudyrock.spring.v5.MongockSpring5.MongockApplicationRunner
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

const val CHANGE_LOG_COLLECTION = "submitter_mongockChangeLog"
const val CHANGE_LOG_LOCK = "submitter_mongockLock"

@Configuration
@ConditionalOnProperty(prefix = "app.persistence", name = ["enableMongo"], havingValue = "true")
@EnableMongoRepositories(
    basePackageClasses = [
        SubmissionMongoRepository::class,
        DocSubmission::class,
        FileListDocFile::class
    ]
)
@EnableConfigurationProperties
class MongoDbConfig(
    @Value("\${spring.data.mongodb.database}") val mongoDatabase: String,
    @Value("\${spring.data.mongodb.uri}") val mongoUri: String,
) : AbstractMongoClientConfiguration() {

    override fun getDatabaseName(): String = mongoDatabase

    @Bean
    @ConditionalOnProperty(prefix = "app.mongo", name = ["execute-migrations"], havingValue = "true")
    fun mongockApplicationRunner(
        springContext: ApplicationContext,
        mongoTemplate: MongoTemplate,
        @Value("\${app.mongo.migration-package}") migrationPackage: String,
    ): ApplicationRunner {
        return createMongockConfig(
            mongoTemplate,
            springContext,
            listOf(
                ChangeSet001::class.java,
                ChangeSet002::class.java,
                ChangeSet003::class.java,
                ChangeSet004::class.java
            )
        )
    }

    @Bean
    override fun mongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    @Bean
    override fun customConversions(): MongoCustomConversions {
        val converters = mutableListOf<Converter<*, *>>()

        converters.add(docSubmissionConverter())
        converters.add(submissionConverter())
        converters.add(FileListDocFileConverter(FileConverter(AttributeConverter())))
        converters.add(DocFileListDocFileConverter(DocFileConverter(DocAttributeConverter())))

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
        return DocSubmissionConverter(docFileConverter, docSectionConverter, docAttributeConverter)
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
        return SubmissionConverter(sectionConverter, attributeConverter, fileConverter)
    }

    companion object {
        fun createMongockConfig(
            mongoTemplate: MongoTemplate,
            springContext: ApplicationContext,
            classes: List<Class<*>>,
        ): MongockApplicationRunner {
            return MongockSpring5.builder()
                .setDriver(createDriver(mongoTemplate))
                //.addChangeLogsScanPackage(migrationPackage)
                .addChangeLogClasses(classes)
                .setSpringContext(springContext)
                .buildApplicationRunner()
        }

        private fun createDriver(mongoTemplate: MongoTemplate): SpringDataMongoV3Driver? {
            val driver = SpringDataMongoV3Driver.withDefaultLock(mongoTemplate)
            driver.lockRepositoryName = CHANGE_LOG_LOCK
            driver.changeLogRepositoryName = CHANGE_LOG_COLLECTION
            return driver
        }
    }
}
