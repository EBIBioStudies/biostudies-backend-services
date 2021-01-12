package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtAttribute
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.attributeDb
import org.junit.jupiter.api.Test

internal class ToExtAttributeTest {

    private val attribute = attributeDb

    @Test
    fun toExtAttribute() {
        val extendedAttribute = attribute.toExtAttribute()
        assertExtAttribute(extendedAttribute)
    }
}
