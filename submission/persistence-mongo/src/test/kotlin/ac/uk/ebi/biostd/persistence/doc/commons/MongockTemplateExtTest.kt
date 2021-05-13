package ac.uk.ebi.biostd.persistence.doc.commons

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import com.github.cloudyrock.mongock.driver.api.lock.guard.invoker.LockGuardInvokerImpl
import com.github.cloudyrock.mongock.driver.core.lock.DefaultLockManager
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import com.github.cloudyrock.mongock.driver.mongodb.sync.v4.repository.MongoSync4LockRepository
import com.github.cloudyrock.mongock.utils.TimeService
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.mongodb.core.collectionExists
import org.springframework.data.mongodb.core.createCollection

@ExtendWith(MockKExtension::class)
class MongockTemplateExtTest {
    val mongockTemplate = spyk<MongockTemplate>()

    @BeforeEach
    fun init() {
        mongockTemplate.collectionNames.toList().forEach { mongockTemplate.dropCollection(it) }
    }

    @Test
    fun `when collection does not exists`() {
        assertThat(mongockTemplate.collectionExists<DocSubmission>()).isFalse()

        mongockTemplate.ensureExists(DocSubmission::class.java)

        assertThat(mongockTemplate.collectionExists<DocSubmission>()).isTrue()
        verify(exactly = 1) { mongockTemplate.collectionExists<DocSubmission>() }
        verify(exactly = 1) { mongockTemplate.createCollection<DocSubmission>() }
    }

    @Test
    fun `when collection already exists`() {
        mongockTemplate.createCollection<DocSubmission>()

        assertThat(mongockTemplate.collectionExists<DocSubmission>()).isTrue()
        assertThat(mongockTemplate.collectionNames.size).isEqualTo(1)

        mongockTemplate.ensureExists(DocSubmission::class.java)

        assertThat(mongockTemplate.collectionExists<DocSubmission>()).isTrue()
        assertThat(mongockTemplate.collectionNames.size).isEqualTo(1)
        verify(exactly = 1) { mongockTemplate.collectionExists<DocSubmission>() }
        verify(exactly = 0) { mongockTemplate.createCollection<DocSubmission>() }
    }
}
