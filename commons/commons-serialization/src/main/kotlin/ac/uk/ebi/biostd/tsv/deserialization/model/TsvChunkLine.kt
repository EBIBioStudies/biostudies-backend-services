package ac.uk.ebi.biostd.tsv.deserialization.model

import ac.uk.ebi.biostd.tsv.TSV_SEPARATOR
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.util.collections.findSecond

class TsvChunkLine(
    val index: Int,
    val body: String,
    private val rawLines: List<String> = body.split(TSV_SEPARATOR),
    private val lines: List<String> = body.split(TSV_SEPARATOR).filter(String::isNotBlank)
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
