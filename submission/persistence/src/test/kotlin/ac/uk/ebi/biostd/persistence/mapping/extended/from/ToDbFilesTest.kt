package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.test.assertDbFiles
import ac.uk.ebi.biostd.persistence.test.extFiles
import org.junit.jupiter.api.Test

internal class ToDbFilesTest {

    @Test
    fun toDbFiles() {
        val extFiles = extFiles
        val dbFiles = extFiles.toDbFiles()

        assertDbFiles(dbFiles)
    }
}
