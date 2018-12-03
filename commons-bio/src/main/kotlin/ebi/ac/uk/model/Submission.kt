package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY
import java.time.OffsetDateTime
import java.util.Objects

open class Submission(
    var accNo: String = "",
    var section: Section = Section(),
    attributes: List<Attribute> = emptyList()
) : Attributable(attributes) {
    var accessTags: MutableList<String> = mutableListOf()
}

class ExtendedSubmission(
    accNo: String,
    val user: User
) : Submission(accNo) {

    constructor(submission: Submission, user: User) : this(submission.accNo, user) {
        section = submission.section
        attributes = submission.attributes
        accessTags = submission.accessTags
    }

    var relPath = EMPTY
    var released = false
    var secretKey = EMPTY

    var releaseTime = OffsetDateTime.now()
    var modificationTime = OffsetDateTime.now()
    var creationTime = OffsetDateTime.now()

    override fun equals(other: Any?) = when {
        other !is Submission -> false
        other === this -> true
        else -> Objects.equals(accNo, other.accNo)
            .and(Objects.equals(section, other.section))
            .and(Objects.equals(attributes, other.attributes))
    }

    override fun hashCode() = Objects.hash(accNo, section, attributes)
}
