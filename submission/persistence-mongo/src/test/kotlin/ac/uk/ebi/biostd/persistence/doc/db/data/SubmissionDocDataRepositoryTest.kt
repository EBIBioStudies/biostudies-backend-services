package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.doc.db.repositories.getByAccNo
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocCollection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.testDocSubmission
import ebi.ac.uk.db.MINIMUM_RUNNING_TIME
import ebi.ac.uk.db.MONGO_VERSION
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds
import java.time.Instant.ofEpochSecond
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
internal class SubmissionDocDataRepositoryTest {
    @Autowired
    lateinit var testInstance: SubmissionDocDataRepository

    @BeforeEach
    fun beforeEach() {
        testInstance.deleteAll()
    }

    @Nested
    inner class ReleaseSubmission {
        @Test
        fun `release submission`() {
            testInstance.save(testDocSubmission.copy(accNo = "S-BIAD1", version = 1, released = false))
            testInstance.setAsReleased("S-BIAD1")

            assertThat(testInstance.getByAccNo(accNo = "S-BIAD1").released).isTrue
        }
    }

    @Nested
    inner class ExpireSubmissions {
        @Test
        fun `expire active processed versions`() {
            testInstance.save(testDocSubmission.copy(accNo = "S-BSST4", version = -1))
            testInstance.save(testDocSubmission.copy(accNo = "S-BSST4", version = 2))

            testInstance.expireActiveProcessedVersions("S-BSST4")

            assertThat(testInstance.getByAccNoAndVersion("S-BSST4", version = -1)).isNotNull
            assertThat(testInstance.getByAccNoAndVersion("S-BSST4", version = -2)).isNotNull
        }
    }

    @Nested
    inner class GetSubmissions {
        @Test
        fun `by email`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo1", owner = "anotherEmail"))
            val doc2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2", owner = "ownerEmail"))

            val result = testInstance.getSubmissions(SubmissionFilter(), "ownerEmail")

            assertThat(result).containsOnly(doc2)
        }

        @Test
        fun `by type`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo1"))
            val doc2 = testInstance.save(
                testDocSubmission.copy(accNo = "accNo2", section = testDocSection.copy(type = "work"))
            )

            val result = testInstance.getSubmissions(SubmissionFilter(type = "work"))

            assertThat(result).containsOnly(doc2)
        }

        @Test
        fun `by AccNo`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo1"))
            val doc2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2"))

            val result = testInstance.getSubmissions(SubmissionFilter(accNo = "accNo2"))

            assertThat(result).containsOnly(doc2)
        }

        @Test
        fun `by release time`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo1", releaseTime = ofEpochSecond(5)))
            val doc2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2", releaseTime = ofEpochSecond(15)))

            val result = testInstance.getSubmissions(
                SubmissionFilter(
                    rTimeFrom = OffsetDateTime.ofInstant(ofEpochSecond(10), ZoneOffset.UTC),
                    rTimeTo = OffsetDateTime.ofInstant(ofEpochSecond(20), ZoneOffset.UTC)
                )
            )

            assertThat(result).containsOnly(doc2)
        }

        @Test
        fun `by keywords`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo1", title = "another"))
            val doc2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2", title = "title"))

            val result = testInstance.getSubmissions(SubmissionFilter(keywords = "title"), null)

            assertThat(result).containsOnly(doc2)
        }

        @Test
        fun `by released`() {
            testInstance.save(testDocSubmission.copy(accNo = "accNo1", released = true))
            val doc2 = testInstance.save(testDocSubmission.copy(accNo = "accNo2", released = false))

            val result = testInstance.getSubmissions(SubmissionFilter(released = false), null)

            assertThat(result).containsOnly(doc2)
        }

        @Test
        fun `by current version`() {
            testInstance.save(testDocSubmission.copy(accNo = "S-BSST3", version = -1))
            testInstance.save(testDocSubmission.copy(accNo = "S-BSST3", version = 2))

            assertThat(testInstance.getCurrentVersion("S-BSST3")).isEqualTo(2)
        }
    }

    @Test
    fun getProjects() {
        testInstance.save(testDocSubmission)

        val projects = testInstance.getCollections(testDocSubmission.accNo)

        assertThat(projects).containsExactly(testDocCollection)
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
