package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertFullExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.fullDocAttribute
import org.junit.jupiter.api.Test

class ToExtAttributeTest {
    @Test
    fun `to ext attribute`() {
        val extAttribute = fullDocAttribute.toExtAttribute()
        assertFullExtAttribute(extAttribute)
    }
}
