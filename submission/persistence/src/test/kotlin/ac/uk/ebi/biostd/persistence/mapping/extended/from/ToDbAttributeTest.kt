package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.test.assertDbAttribute
import ac.uk.ebi.biostd.persistence.test.extAttribute
import org.junit.jupiter.api.Test

internal class ToDbAttributeTest {

    @Test
    fun toDbAttribute() {
        val attribute = extAttribute
        val dbAttribute = attribute.toDbAttribute(1)

        assertDbAttribute(dbAttribute, attribute)
    }
}
