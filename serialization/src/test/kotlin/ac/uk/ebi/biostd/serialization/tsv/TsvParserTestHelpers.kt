package ac.uk.ebi.biostd.serialization.tsv

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions

internal class TsvString(private val lines: List<String>) {
    constructor(tsvString: String) : this(tsvString.split("\n"))

    operator fun get(row: Int, column: Int) = lines[row].split("\t")[column]
    operator fun get(row: Int) = TabRow(lines[row].split("\t"))
}

internal class TabRow(val data: List<String>)

internal fun assertThat(tabRow: TabRow): TabRowAssert = TabRowAssert(tabRow)

internal class TabRowAssert(actual: TabRow) : AbstractAssert<TabRowAssert, TabRow>(actual, TabRowAssert::class.java) {

    fun contains(row1: String) {
        Assertions.assertThat(actual.data[0]).contains(row1)
    }

    fun contains(row1: String, row2: String) {
        Assertions.assertThat(actual.data[0]).contains(row1)
        Assertions.assertThat(actual.data[1]).contains(row2)
    }

    fun contains(row1: String, row2: String, row3: String) {
        contains(row1, row2)
        Assertions.assertThat(actual.data[2]).contains(row3)
    }

    fun contains(row1: String, row2: String, row3: String, row4: String) {
        contains(row1, row2, row3)
        Assertions.assertThat(actual.data[3]).contains(row4)
    }

    fun contains(row1: String, row2: String, row3: String, row4: String, row5: String) {
        contains(row1, row2, row3, row4)
        Assertions.assertThat(actual.data[4]).contains(row5)
    }

    fun isEmptyLine() {
        Assertions.assertThat(actual.data[0]).isBlank()
    }

}
