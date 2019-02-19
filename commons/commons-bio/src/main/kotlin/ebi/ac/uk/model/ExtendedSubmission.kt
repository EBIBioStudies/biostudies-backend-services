package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY
import java.time.OffsetDateTime
import java.util.Objects

class ExtendedSubmission(accNo: String, val user: User) : Submission(accNo) {

    constructor(submission: Submission, user: User) : this(submission.accNo, user) {
        section = submission.section
        attributes = submission.attributes
        accessTags = submission.accessTags
    }

    var relPath = EMPTY
    var released = false
    var secretKey = EMPTY
    var version = 1
    var releaseTime: OffsetDateTime = OffsetDateTime.now()
    var modificationTime: OffsetDateTime = OffsetDateTime.now()
    var creationTime: OffsetDateTime = OffsetDateTime.now()

    override fun equals(other: Any?) = when {
        other !is ExtendedSubmission -> false
        other === this -> true
        else -> Objects.equals(accNo, other.accNo)
            .and(Objects.equals(version, other.version))
            .and(Objects.equals(section, other.section))
            .and(Objects.equals(attributes, other.attributes))
    }

    override fun hashCode() = Objects.hash(accNo, section, attributes)
}
