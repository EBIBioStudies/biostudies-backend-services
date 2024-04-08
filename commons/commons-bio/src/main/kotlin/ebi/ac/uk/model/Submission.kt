package ebi.ac.uk.model

import java.util.Objects
import java.util.Objects.equals

class Submission(
    var accNo: String = "",
    var section: Section = Section(),
    var tags: MutableList<Pair<String, String>> = mutableListOf(),
    override var attributes: List<Attribute> = listOf(),
) : Attributable {
    override fun equals(other: Any?) =
        when {
            other !is Submission -> false
            other === this -> true
            else ->
                equals(accNo, other.accNo)
                    .and(equals(section, other.section))
                    .and(equals(attributes, other.attributes))
        }

    override fun hashCode() = Objects.hash(accNo, section, attributes)

    override fun toString() = "Submission(accNo='$accNo',attributes=$attributes)"
}
