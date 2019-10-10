package ebi.ac.uk.model

import java.util.Objects

open class Submission(
    var accNo: String = "",
    var section: Section = Section(),
    var tags: MutableList<Pair<String, String>> = mutableListOf(),
    var accessTags: MutableList<String> = mutableListOf(),
    override var attributes: List<Attribute> = listOf()
) : Attributable {
    override fun equals(other: Any?) = when {
        other !is Submission -> false
        other === this -> true
        else -> (accNo == other.accNo)
            .and(section == other.section)
            .and(accessTags == other.accessTags)
            .and(attributes == other.attributes)
    }

    override fun hashCode() = Objects.hash(accNo, section, attributes)
}
