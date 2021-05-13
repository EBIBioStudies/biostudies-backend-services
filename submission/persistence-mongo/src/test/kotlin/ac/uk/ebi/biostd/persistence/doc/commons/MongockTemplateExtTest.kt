package ac.uk.ebi.biostd.persistence.doc.commons

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.integration.SerializationConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.doc.commons.MongockTemplateExtTest.TestConfigMongock
import ac.uk.ebi.biostd.persistence.doc.integration.MongoDbServicesConfig
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import com.github.cloudyrock.mongock.driver.api.lock.LockManager
import com.github.cloudyrock.mongock.driver.api.lock.guard.invoker.LockGuardInvoker
import com.github.cloudyrock.mongock.driver.api.lock.guard.invoker.LockGuardInvokerImpl
import com.github.cloudyrock.mongock.driver.core.lock.DefaultLockManager
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import com.github.cloudyrock.mongock.driver.mongodb.sync.v4.repository.MongoSync4LockRepository
import com.github.cloudyrock.mongock.utils.TimeService
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.collectionExists
import org.springframework.data.mongodb.core.createCollection
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.nio.file.Files

@ExtendWith(MockKExtension::class)
@Import(TestConfigMongock::class)
class MongockTemplateExtTest(
        @Autowired private val mongockTemplate: MongockTemplate
) {
    @BeforeEach
    fun init() {
        mongockTemplate.collectionNames.toList().forEach { mongockTemplate.dropCollection(it) }
    }

    @Test
    fun `when collection does not exists`() {
        assertThat(mongockTemplate.collectionExists<DocSubmission>()).isFalse()

        mongockTemplate.ensureExists(DocSubmission::class.java)

        assertThat(mongockTemplate.collectionExists<DocSubmission>()).isTrue()
    }

    @Test
    fun `when collection already exists`() {
        mongockTemplate.createCollection<DocSubmission>()

        assertThat(mongockTemplate.collectionExists<DocSubmission>()).isTrue()
        assertThat(mongockTemplate.collectionNames.size).isEqualTo(1)

        mongockTemplate.ensureExists(DocSubmission::class.java)

        assertThat(mongockTemplate.collectionExists<DocSubmission>()).isTrue()
        assertThat(mongockTemplate.collectionNames.size).isEqualTo(1)
    }

    @Configuration
    class TestConfigMongock(@Autowired private val mongoTemplate: MongoTemplate) {
        @Bean
        fun mongockTemplate() = MongockTemplate(mongoTemplate,
                LockGuardInvokerImpl(DefaultLockManager(MongoSync4LockRepository(
                        mongoTemplate.createCollection(DocSubmission::class.java),
                        true),
                        TimeService())))
    }
}
