package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertFullExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.fullDocAttribute
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ToExtAttributeTest {
    @Test
    fun `to ext attribute`() {
        val extAttribute = fullDocAttribute.toExtAttribute()
        assertFullExtAttribute(extAttribute)
    }

    @Test
    fun `to ext attributes`() {
        val extAttributes = listOf(fullDocAttribute).toExtAttributes()
        assertThat(extAttributes).hasSize(1)
        assertFullExtAttribute(extAttributes.first())
    }
}
