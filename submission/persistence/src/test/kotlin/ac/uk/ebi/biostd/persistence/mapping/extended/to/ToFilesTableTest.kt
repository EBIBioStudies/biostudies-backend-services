package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.fileDb
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.second
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

private const val FILE_1 = "file1"
private const val FILE_2 = "file1"
private const val FILE_3 = "file1"

@Nested
@ExtendWith(MockKExtension::class)
class ToFilesTableTest {
    private val filesSource = mockk<FilesSource>()
    private val simpleExtFile = mockk<File>()

    private val file = fileDb.apply { name = FILE_1; order = 0; tableIndex = NO_TABLE_INDEX }
    private val tableFile = fileDb.apply { name = FILE_2; order = 1; tableIndex = 0; }
    private val anotherTableFile = fileDb.apply { name = FILE_3; order = 2; tableIndex = 1; }

    private val files = sortedSetOf(file, tableFile, anotherTableFile)

    @Test
    fun `Files to ExtFileTable`() {
        every { filesSource.getFile(file.name) } returns simpleExtFile

        val files = files.toExtFiles(filesSource)

        assertThat(files.first()).isEqualTo(Either.left(file.toExtFile(filesSource)))
        assertThat(files.second()).isEqualTo(Either.right(ExtFileTable(
            tableFile.toExtFile(filesSource), anotherTableFile.toExtFile(filesSource))))
    }

}
