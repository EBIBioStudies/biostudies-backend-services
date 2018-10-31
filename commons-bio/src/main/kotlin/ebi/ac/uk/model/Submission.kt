package ebi.ac.uk.model

import ebi.ac.uk.model.extensions.Section

class Submission(
        var accNo: String = "",
        var rootSection: Section = Section(),
        var accessTags: MutableList<String> = mutableListOf(),
        attributes: List<Attribute> = emptyList()) : Attributable(attributes)