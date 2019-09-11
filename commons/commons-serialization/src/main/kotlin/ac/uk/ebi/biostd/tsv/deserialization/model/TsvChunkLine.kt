package ac.uk.ebi.biostd.tsv.deserialization.model

import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.util.collections.findSecond

class TsvChunkLine(
    val index: Int,
    val rawLines: List<String>,
    private val lines: List<String> = rawLines.filter(String::isNotBlank)
) : List<String> by lines {

    val value: String
        get() {
            return lines.findSecond().fold({ EMPTY }, { it })
        }

    val rawValues: List<String>
        get() {
            return rawLines.drop(1)
        }

    val name: String
        get() {
            return lines.first()
        }
}
