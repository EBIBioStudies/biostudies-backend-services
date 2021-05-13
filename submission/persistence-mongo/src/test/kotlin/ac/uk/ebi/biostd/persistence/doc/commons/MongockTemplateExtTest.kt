package ac.uk.ebi.biostd.persistence.doc.commons

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.mongodb.core.collectionExists
import org.springframework.data.mongodb.core.createCollection

@ExtendWith(MockKExtension::class)
class MongockTemplateExtTest {
    val mongockTemplate = mockk<MongockTemplate>()

    @Test
    fun `when collection does not exists`() {
        every { mongockTemplate.collectionExists<DocSubmission>() } returns false
        every { mongockTemplate.createCollection<DocSubmission>() } answers { mockk() }

        mongockTemplate.ensureExists(DocSubmission::class.java)

        verify(exactly = 1) { mongockTemplate.createCollection<DocSubmission>() }
    }

    @Test
    fun `when collection already exists`() {
        every { mongockTemplate.collectionExists<DocSubmission>() } returns true

        mongockTemplate.ensureExists(DocSubmission::class.java)

        verify(exactly = 0) { mongockTemplate.createCollection<DocSubmission>() }
    }
}
