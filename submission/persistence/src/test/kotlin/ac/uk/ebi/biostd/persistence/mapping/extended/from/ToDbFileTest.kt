package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.test.assertDbFile
import ac.uk.ebi.biostd.persistence.test.extTestFile
import org.junit.jupiter.api.Test

internal class ToDbFileTest {
    @Test
    fun `toDbFile list file`() {
        val extFile = extTestFile
        val dbFile = extTestFile.toDbFile(1)

        assertDbFile(dbFile, extFile, 1)
    }

    @Test
    fun `toDbFile table file`() {
        val extFile = extTestFile
        val dbFile = extTestFile.toDbFile(1, 5)

        assertDbFile(dbFile, extFile, 1, 5)
    }
}
