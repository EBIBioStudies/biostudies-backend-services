package ac.uk.ebi.pmc.migrations

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MongockTemplateExt2Test {
    private val mongockTemplate = mockk<MongockTemplate>()

    private val collectionName = "collectionName"

    @Test
    fun `when collection does not exists`() {
        every { mongockTemplate.collectionExists(collectionName) } returns false
        every { mongockTemplate.createCollection(collectionName) } answers { mockk() }

        mongockTemplate.createCollectionByNameIfNotExists(collectionName)
    }

    @Test
    fun `when collection already exists`() {
        every { mongockTemplate.collectionExists(collectionName) } returns true

        mongockTemplate.createCollectionByNameIfNotExists(collectionName)
    }
}
