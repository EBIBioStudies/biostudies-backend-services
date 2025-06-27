package ac.uk.ebi.biostd.tsv.deserialization.model

import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import ebi.ac.uk.util.collections.findSecond

class TsvChunkLine(
    val index: Int,
    private val rawLines: List<String?> = emptyList(),
) : List<String?> by rawLines {
    val rawValues: List<String?>
        get() {
            return rawLines.drop(1)
        }

    val name: String
        get() {
            return rawLines.first() ?: throw InvalidElementException(REQUIRED_ATTR_NAME)
        }

    val value: String?
        get() {
            return rawLines.findSecond()
        }
}
