package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.CollectionWithoutPatternException
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbQueryConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.service.SubmissionMongoMetaQueryServiceTest.PropertyOverrideContextInitializer
import ac.uk.ebi.biostd.persistence.doc.test.beans.TestConfig
import ac.uk.ebi.biostd.persistence.doc.test.doc.COLLECTION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import ebi.ac.uk.model.constants.SubFields.COLLECTION_VALIDATOR
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ebi.ac.uk.asserts.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.nio.file.Files
import java.time.Duration.ofSeconds
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbQueryConfig::class, TestConfig::class])
@ContextConfiguration(initializers = [PropertyOverrideContextInitializer::class])
internal class SubmissionMongoMetaQueryServiceTest(
    @Autowired private val submissionMongoRepository: SubmissionDocDataRepository,
    @Autowired private val testInstance: SubmissionMongoMetaQueryService,
) {
    @Test
    fun getBasicCollection() = runTest {
        submissionMongoRepository.save(
            testDocSubmission.copy(
                accNo = "EuToxRisk",
                version = 1,
                attributes = listOf(
                    DocAttribute(ACC_NO_TEMPLATE.value, "!{S-TOX}"),
                    DocAttribute(COLLECTION_VALIDATOR.value, "EuToxRiskValidator")
                )
            )
        )

        val (accNo, accNoPattern, collections, validator, releaseTime) = testInstance.getBasicCollection("EuToxRisk")
        assertThat(accNo).isEqualTo("EuToxRisk")
        assertThat(accNoPattern).isEqualTo("!{S-TOX}")
        assertThat(validator).isEqualTo("EuToxRiskValidator")
        assertThat(collections).containsExactly(COLLECTION_ACC_NO)
        assertThat(releaseTime).isEqualTo(RELEASE_TIME.atOffset(UTC).truncatedTo(ChronoUnit.MILLIS))
    }

    @Test
    fun `non existing collection`() = runTest {
        val error = assertThrows<CollectionNotFoundException> { testInstance.getBasicCollection("NonExisting") }
        assertThat(error.message).isEqualTo("The collection 'NonExisting' was not found")
    }

    @Test
    fun `collection without pattern`() = runTest {
        submissionMongoRepository.save(
            testDocSubmission.copy(
                accNo = "PatternLess",
                version = 1,
                attributes = listOf(DocAttribute(COLLECTION_VALIDATOR.value, "PatternLessValidator"))
            )
        )

        val error = assertThrows<CollectionWithoutPatternException> { testInstance.getBasicCollection("PatternLess") }
        assertThat(error.message).isEqualTo("The collection 'PatternLess' does not have a valid accession pattern")
    }

    @Test
    fun findLatestBasicByAccNo() = runTest {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo2", version = -1))
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo2", version = -2))
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo2", version = 4))

        val lastVersion = testInstance.findLatestBasicByAccNo("accNo2")

        assertThat(lastVersion).isNotNull
        assertThat(lastVersion!!.version).isEqualTo(4)
    }

    @Test
    fun `exists by AccNo when exists`() = runTest {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo3", version = 1))

        assertThat(submissionMongoRepository.existsByAccNo("accNo3")).isTrue
    }

    @Test
    fun `exist by AccNo when don't exists`() = runTest {
        submissionMongoRepository.save(testDocSubmission.copy(accNo = "accNo4", version = 1))

        assertThat(submissionMongoRepository.existsByAccNo("accNo5")).isFalse
    }

    class PropertyOverrideContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(context: ConfigurableApplicationContext) {
            val path = Files.createTempDirectory("mongo_test").toString()
            addInlinedPropertiesToEnvironment(context, "app.submissionPath=$path")
        }
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
            .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}
