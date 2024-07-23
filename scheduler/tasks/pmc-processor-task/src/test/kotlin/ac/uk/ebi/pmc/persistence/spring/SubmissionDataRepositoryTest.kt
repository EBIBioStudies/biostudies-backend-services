package ac.uk.ebi.pmc.persistence.spring

import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.DISCARDED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.LOADED
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus.PROCESSING
import ac.uk.ebi.pmc.persistence.repository.SubmissionDataRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionDocRepository
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReactiveConfig::class])
class SubmissionDataRepositoryTest(
    @Autowired private val testInstance: SubmissionDataRepository,
    @Autowired private val docRepository: SubmissionDocRepository,
) {
    @BeforeEach
    fun beforeEach() =
        runBlocking {
            docRepository.deleteAll()
        }

    @Nested
    inner class FindAndUpdate {
        @Test
        fun findAndUpdateWhenNoRecords() =
            runTest {
                assertThat(testInstance.findAndUpdate(LOADED, PROCESSING)).isNull()
            }

        @Test
        fun findAndUpdate() =
            runTest {
                val sub1 =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 1,
                        sourceTime = 2024_01_01_1,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )
                docRepository.save(sub1)
                val sub2 =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 1,
                        sourceTime = 2024_01_01_1,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )
                docRepository.save(sub2)

                val result =
                    buildList {
                        add(testInstance.findAndUpdate(LOADED, PROCESSING))
                        add(testInstance.findAndUpdate(LOADED, PROCESSING))
                    }
                val expected =
                    buildList {
                        add(sub1.copy(status = PROCESSING))
                        add(sub2.copy(status = PROCESSING))
                    }
                assertThat(result).containsExactlyInAnyOrderElementsOf(expected)
                assertThat(docRepository.findAll().toList()).containsExactlyInAnyOrderElementsOf(expected)
            }
    }

    @Nested
    inner class SaveNew {
        @Test
        fun `save when no others records`() =
            runTest {
                val newSub =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 1,
                        sourceTime = 2024_01_01_0,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )

                val result = testInstance.saveNew(newSub)
                assertThat(result).isTrue()
                assertThat(docRepository.findAll().toList()).containsOnly(newSub)
            }

        @Test
        fun `save new when an more recent version exists`(): Unit =
            runTest {
                val sub =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 1,
                        sourceTime = 2024_01_01_1,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )
                docRepository.save(sub)

                val newSub =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 1,
                        sourceTime = 2024_01_01_0,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )

                val result = testInstance.saveNew(newSub)
                assertThat(result).isFalse()

                assertThat(docRepository.findAll().toList()).containsOnly(sub)
            }

        @Test
        fun `save new when an more recent version exists because position in file`(): Unit =
            runTest {
                val sub =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 2,
                        sourceTime = 2024_01_01_1,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )
                docRepository.save(sub)

                val newSub =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 1,
                        sourceTime = 2024_01_01_1,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )

                val result = testInstance.saveNew(newSub)
                assertThat(result).isFalse()

                assertThat(docRepository.findAll().toList()).containsOnly(sub)
            }

        @Test
        fun `save new when an more recent version does not exists`(): Unit =
            runTest {
                val sub =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 1,
                        sourceTime = 2024_01_01_1,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )
                docRepository.save(sub)

                val newSub =
                    SubmissionDocument(
                        id = ObjectId(),
                        accNo = "abc123",
                        body = "pageTab",
                        status = LOADED,
                        sourceFile = "/abc.txt",
                        posInFile = 1,
                        sourceTime = 2024_01_02_0,
                        files = emptyList(),
                        updated = Instant.now().truncatedTo(SECONDS),
                    )

                val result = testInstance.saveNew(newSub)
                assertThat(result).isTrue()

                assertThat(docRepository.findAll().toList()).containsOnly(newSub, sub.copy(status = DISCARDED))
            }
    }

    companion object {
        @Container
        val mongoContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse(MONGO_VERSION))
                .withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(ofSeconds(MINIMUM_RUNNING_TIME)))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("biostudies-test") }
            register.add("spring.data.mongodb.database") { "biostudies-test" }
        }
    }
}

@Configuration
@EnableConfigurationProperties
@EnableReactiveMongoRepositories(
    basePackageClasses = [
        SubmissionDocRepository::class,
    ],
)
class MongoDbReactiveConfig(
    @Value("\${spring.data.mongodb.database}") val mongoDatabase: String,
    @Value("\${spring.data.mongodb.uri}") val mongoUri: String,
) : AbstractReactiveMongoConfiguration() {
    override fun getDatabaseName(): String = mongoDatabase

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        return MongoClients.create(mongoUri)
    }

    @Bean
    fun submissionDataRepository(
        submissionDocRepository: SubmissionDocRepository,
        mongoTemplate: ReactiveMongoTemplate,
    ): SubmissionDataRepository {
        return SubmissionDataRepository(submissionDocRepository, mongoTemplate)
    }
}
