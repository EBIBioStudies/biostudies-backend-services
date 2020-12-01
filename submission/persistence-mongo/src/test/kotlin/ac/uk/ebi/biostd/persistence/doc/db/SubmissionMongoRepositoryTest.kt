package ac.uk.ebi.biostd.persistence.doc.db

import ac.uk.ebi.biostd.persistence.doc.MongoConfig
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.test.DocTestFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [MongoConfig::class])
class SubmissionMongoRepositoryTest(
    @Autowired
    val submissionRepository: SubmissionDocDataRepository,

    @Autowired
    val template: MongoTemplate) {

    @Nested
    inner class TestSavingDoc {
        @Test
        fun saveSubmission() {
            val response = submissionRepository.save(DocTestFactory.docSubmission)
            val elements = template.getCollection("submissions").find().toList()

            //val valid = submissionRepository.findById(response.id!!)
            //  assertThat(valid.get()).isEqualTo(response)
        }
    }

    @Nested
    inner class TestGetNextVersion {

        @Test
        fun whenDoesNotExists() {
            val next = submissionRepository.getCurrentVersion("abc-123")
            assertThat(next).isNull()
        }

        @Test
        fun whenExists() {
            submissionRepository.save(DocTestFactory.docSubmission.copy(accNo = "abc-234", version = 2))

            val current = submissionRepository.getCurrentVersion("abc-234")
            assertThat(current).isEqualTo(2)
        }

    }

    companion object {
        var mongoDBContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            mongoDBContainer.start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun mongoDbProperties(registry: DynamicPropertyRegistry) {
            registry.add("app.connection") { mongoDBContainer.getReplicaSetUrl() }
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            mongoDBContainer.stop()
        }
    }
}


