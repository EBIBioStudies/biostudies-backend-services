package ebi.ac.uk.model

open class Submission(
    var accNo: String = "",
    var section: Section = Section(),
    attributes: List<Attribute> = emptyList()
) : Attributable(attributes) {

    var accessTags: MutableList<String> = mutableListOf()
}
