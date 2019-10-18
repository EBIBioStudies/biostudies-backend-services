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
        else -> Objects.equals(accNo, other.accNo)
            .and(Objects.equals(section, other.section))
            .and(Objects.equals(accessTags, other.accessTags))
            .and(Objects.equals(attributes, other.attributes))
    }

    override fun hashCode() = Objects.hash(accNo, section, attributes)
}
