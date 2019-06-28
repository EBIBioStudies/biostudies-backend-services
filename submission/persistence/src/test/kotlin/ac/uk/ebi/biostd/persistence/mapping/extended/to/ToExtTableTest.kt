package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.extTestFile
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.simpleFile
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.io.FilesSource
import ebi.ac.uk.util.collections.second
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToExtTableTest(
    @MockK val filesSource: FilesSource,
    @MockK val tableExtFile: ExtFile,
    @MockK val anotherExtFile: ExtFile) {

    private val file = simpleFile.apply { tableIndex = NO_TABLE_INDEX; order = 0 }
    private val tableFile = simpleFile.apply { tableIndex = 0; order = 1 }
    private val anotherTableFile = simpleFile.apply { tableIndex = 1; order = 2 }
    private val files = sortedSetOf(file, tableFile, anotherTableFile)

    @Test
    fun `Files to ExtFileTable`() {
        mockkStatic(TO_EXT_FILE_EXTENSIONS) {
            every { file.toExtFile(filesSource) } returns extTestFile
            every { tableFile.toExtFile(filesSource) } returns tableExtFile
            every { anotherTableFile.toExtFile(filesSource) } returns anotherExtFile

            val files = files.toExtFiles(filesSource)
            assertThat(files.first()).isEqualTo(Either.left(extTestFile))
            assertThat(files.second()).isEqualTo(Either.right(ExtFileTable(listOf(tableExtFile, anotherExtFile))))
        }
    }
}
