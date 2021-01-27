package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionMongoRepository
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbReposConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ebi.ac.uk.model.constants.SubFields
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Instant

@ExtendWith(SpringExtension::class)
@Testcontainers
@SpringBootTest(classes = [MongoDbReposConfig::class])
internal class SubmissionMongoMetaQueryServiceTest {

    @Autowired
    lateinit var testInstance: SubmissionMongoMetaQueryService

    @Autowired
    lateinit var submissionMongoRepository: SubmissionMongoRepository

    @Test
    fun getBasicProject() {
        submissionMongoRepository.save(testDocSubmission(
            accNo = "accNo1",
            version = 1,
            status = DocProcessingStatus.PROCESSED,
            attributes = listOf(DocAttribute(SubFields.ACC_NO_TEMPLATE.value, "template"))
        ))

        val result = testInstance.getBasicProject("accNo1")

        assertThat(result.accNo).isEqualTo("accNo1")
    }

    @Test
    fun findLatestBasicByAccNo() {
        submissionMongoRepository.save(testDocSubmission("accNo2", 1, DocProcessingStatus.PROCESSED))
        submissionMongoRepository.save(testDocSubmission("accNo2", -2, DocProcessingStatus.PROCESSED))
        submissionMongoRepository.save(testDocSubmission("accNo2", 4, DocProcessingStatus.PROCESSED))

        val lastVersion = testInstance.findLatestBasicByAccNo("accNo2")

        assertThat(lastVersion).isNotNull()
        assertThat(lastVersion!!.version).isEqualTo(4)
    }

    @Test
    fun getAccessTags() {
    }

    @Test
    fun `exists by AccNo when exists`() {
        submissionMongoRepository.save(testDocSubmission("accNo3", 1, DocProcessingStatus.PROCESSED))

        assertThat(submissionMongoRepository.existsByAccNo("accNo3")).isTrue()
    }

    @Test
    fun `exist by AccNo when don't exists`() {
        submissionMongoRepository.save(testDocSubmission("accNo4", 1, DocProcessingStatus.PROCESSED))

        assertThat(submissionMongoRepository.existsByAccNo("accNo5")).isFalse()
    }

    private fun testDocSubmission(
        accNo: String,
        version: Int,
        status: DocProcessingStatus,
        attributes: List<DocAttribute> = listOf()
    ): DocSubmission {
        return DocSubmission(
            id = "",
            accNo = accNo,
            version = version,
            owner = "",
            submitter = "",
            title = "",
            method = DocSubmissionMethod.PAGE_TAB,
            relPath = "",
            rootPath = "",
            released = true,
            secretKey = "",
            status = status,
            releaseTime = Instant.ofEpochSecond(1),
            modificationTime = Instant.ofEpochSecond(2),
            creationTime = Instant.ofEpochSecond(3),
            section = DocSection(type = ""),
            attributes = attributes,
            tags = listOf(),
            projects = listOf(),
            stats = listOf()
        )
    }

    companion object {

        @Container
        val mongoContainer: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))

        @JvmStatic
        @DynamicPropertySource
        fun propertySource(register: DynamicPropertyRegistry) {
            register.add("spring.data.mongodb.uri") { mongoContainer.getReplicaSetUrl("testDb") }
            register.add("spring.data.mongodb.database") { "testDb" }
            register.add("app.persistence.enableMongo") { "true" }
        }
    }
}
