package ebi.ac.uk.asserts

import arrow.core.Either
import ebi.ac.uk.model.Table
import ebi.ac.uk.util.collections.ifRight
import org.assertj.core.api.Assertions

// TODO implement assertLeft (look for better name)
// TODO change all the assertions for these two
// TODO implement equals in submission
// TODO remove the submission assertion if necessary
fun <A, B : Table<A>> assertTable(table: Either<A, B>, vararg expectedRows: A) {
    table.ifRight {
        Assertions.assertThat(it.elements).hasSize(expectedRows.size)
        it.elements.forEachIndexed { idx, rowElement -> Assertions.assertThat(rowElement).isEqualTo(expectedRows[idx]) }
    }
}
