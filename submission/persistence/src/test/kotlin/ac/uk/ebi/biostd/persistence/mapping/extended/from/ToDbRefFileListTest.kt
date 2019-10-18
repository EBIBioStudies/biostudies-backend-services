package ac.uk.ebi.biostd.persistence.mapping.extended.from

import ac.uk.ebi.biostd.persistence.test.assertDbRefFiles
import ac.uk.ebi.biostd.persistence.test.extFileList
import org.junit.jupiter.api.Test

internal class ToDbRefFileListTest {
    @Test
    fun toDbFileList() {
        val extFiles = extFileList
        val dbFileList = extFiles.toDbFileList()

        assertDbRefFiles(dbFileList)
    }
}
