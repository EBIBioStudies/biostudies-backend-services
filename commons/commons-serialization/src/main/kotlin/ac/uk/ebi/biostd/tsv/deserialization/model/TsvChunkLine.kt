package ac.uk.ebi.biostd.tsv.deserialization.model

import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.util.collections.findSecond

class TsvChunkLine(
    val index: Int,
    private val rawLines: List<String> = emptyList()
) : List<String> by rawLines {

    val value: String
        get() {
            return rawLines.findSecond().fold({ EMPTY }, { it })
        }

    val rawValues: List<String>
        get() {
            return rawLines.drop(1)
        }

    val name: String
        get() {
            return rawLines.first()
        }
}
