package ac.uk.ebi.biostd.tsv.deserialization.model

import ac.uk.ebi.biostd.tsv.TSV_SEPARATOR
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.util.collections.findSecond

class TsvChunkLine(
    val index: Int,
    val body: String,
    private val lines: List<String> = body.split(TSV_SEPARATOR).filter(String::isNotBlank)
) : List<String> by lines {

    val value: String
        get() {
            return lines.findSecond().fold({ EMPTY }, { it })
        }

    val values: List<String>
        get() {
            return lines.drop(1)
        }

    val name: String
        get() {
            return lines.first()
        }
}
