package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbServicesConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoMetaQueryServiceTest.TestConfig
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.model.constants.SubFields
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.nio.file.Files

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [TestConfig::class])
internal class SubmissionMongoMetaQueryServiceTest(
    @Autowired val submissionMongoRepository: SubmissionMongoRepository,
    @Autowired val testInstance: SubmissionMongoMetaQueryService
) {

    @Test
    fun getBasicProject() {
        submissionMongoRepository.save(testDocSubmission.copy(
            accNo = "accNo1",
            version = 1,
            status = PROCESSED,
            attributes = listOf(DocAttribute(SubFields.ACC_NO_TEMPLATE.value, "template"))
        ))

        val result = testInstance.getBasicCollection("accNo1")

        assertThat(result.accNo).isEqualTo("accNo1")
    }

    @Test
    fun findLatestBasicByAccNo() {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo2", version = 1, status = PROCESSED))
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo2", version = -2, status = PROCESSED))
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo2", version = 4, status = PROCESSED))

        val lastVersion = testInstance.findLatestBasicByAccNo("accNo2")

        assertThat(lastVersion).isNotNull()
        assertThat(lastVersion!!.version).isEqualTo(4)
    }

    @Test
    fun `exists by AccNo when exists`() {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo3", version = 1, status = PROCESSED))

        assertThat(submissionMongoRepository.existsByAccNo("accNo3")).isTrue()
    }

    @Test
    fun `exist by AccNo when don't exists`() {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo4", version = 1, status = PROCESSED))

        assertThat(submissionMongoRepository.existsByAccNo("accNo5")).isFalse()
    }

    @Configuration
    @Import(MongoDbServicesConfig::class)
    class TestConfig {

        @Bean
        fun applicationProperties(): ApplicationProperties {
            val properties = ApplicationProperties()
            properties.submissionPath = Files.createTempDirectory("mongo_test").toString()
            return properties
        }

        @Bean
        fun extSerializationService(): ExtSerializationService = ExtSerializationService()

        @Bean
        fun serializationService(): SerializationService = SerializationConfig.serializationService()
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
            register.add("app.persistence.enableMongo") { "true" }
        }
    }
}
