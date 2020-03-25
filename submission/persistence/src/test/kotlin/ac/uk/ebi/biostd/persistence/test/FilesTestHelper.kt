package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.model.DbFile
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFileTable
import org.assertj.core.api.Assertions.assertThat
import java.util.SortedSet

internal val extFileTable
    get() = ExtFileTable(listOf(extTestFile, extTestFile))

internal val extFiles
    get() = listOf(right(extFileTable), left(extTestFile), right(extFileTable))

internal fun assertDbFiles(files: SortedSet<DbFile>) {
    val fileList = files.toList()
    assertThat(fileList).hasSize(5)

    assertDbFile(fileList[0], extTestFile, order = 0, tableOrder = 0)
    assertDbFile(fileList[1], extTestFile, order = 1, tableOrder = 1)
    assertDbFile(fileList[2], extTestFile, order = 2, tableOrder = -1)
    assertDbFile(fileList[3], extTestFile, order = 3, tableOrder = 0)
    assertDbFile(fileList[4], extTestFile, order = 4, tableOrder = 1)
}
