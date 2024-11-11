package ac.uk.ebi.pmc.config

import ac.uk.ebi.pmc.migrations.executeMigrations
import ac.uk.ebi.pmc.persistence.repository.ErrorsDataRepository
import ac.uk.ebi.pmc.persistence.repository.ErrorsDocRepository
import ac.uk.ebi.pmc.persistence.repository.InputFilesDataRepository
import ac.uk.ebi.pmc.persistence.repository.InputFilesDocRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileDataRepository
import ac.uk.ebi.pmc.persistence.repository.SubFileDocRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionDataRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionDocRepository
import ac.uk.ebi.scheduler.properties.PmcImporterProperties
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import kotlinx.coroutines.runBlocking
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

const val ERRORS_COL = "pmc_errors"
const val SUBMISSION_COL = "pmc_submissions"
const val SUB_FILES_COL = "pmc_submissions_files"
const val INPUT_FILES_COL = "pmc_input_files"

@Configuration
@EnableReactiveMongoRepositories(
    basePackageClasses = [
        SubmissionDocRepository::class,
    ],
)
class PersistenceConfig(val properties: PmcImporterProperties) : AbstractReactiveMongoConfiguration() {
    override fun getDatabaseName(): String = properties.mongodbDatabase

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        return MongoClients.create(properties.mongodbUri)
    }

    @Bean
    fun errorsDataRepository(errorsDocRepository: ErrorsDocRepository): ErrorsDataRepository {
        return ErrorsDataRepository(errorsDocRepository)
    }

    @Bean
    fun inputFilesDataRepository(inputFilesDocRepository: InputFilesDocRepository): InputFilesDataRepository {
        return InputFilesDataRepository(inputFilesDocRepository)
    }

    @Bean
    fun subFileDataRepository(subFileDocRepository: SubFileDocRepository): SubFileDataRepository {
        return SubFileDataRepository(subFileDocRepository)
    }

    @Bean
    fun submissionDataRepository(
        submissionDocRepository: SubmissionDocRepository,
        mongoTemplate: ReactiveMongoTemplate,
    ): SubmissionDataRepository {
        return SubmissionDataRepository(submissionDocRepository, mongoTemplate)
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.mongo", name = ["execute-migrations"], havingValue = "true")
    fun createApplicationRunner(reactiveMongoClient: ReactiveMongoTemplate): ApplicationRunner {
        return object : ApplicationRunner {
            override fun run(args: ApplicationArguments) {
                runBlocking {
                    reactiveMongoClient.executeMigrations()
                }
            }
        }
    }
}
