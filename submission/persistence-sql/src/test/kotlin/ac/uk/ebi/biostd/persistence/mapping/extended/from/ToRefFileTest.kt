package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.test.assertDbRefFile
import ac.uk.ebi.biostd.persistence.test.extTestRefFile
import org.junit.jupiter.api.Test

internal class ToRefFileTest {
    @Test
    fun toRefFile() {
        val refFile = extTestRefFile
        val dbRefFile = extTestRefFile.toRefFile(1)

        assertDbRefFile(dbRefFile, refFile, 1)
    }
}
