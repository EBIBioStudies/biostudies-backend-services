package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import java.time.OffsetDateTime
import java.util.Objects

class ExtendedSubmission(accNo: String, val user: User) : Submission(accNo) {
    constructor(submission: Submission, user: User) : this(submission.accNo, user) {
        section = submission.section
        attributes = submission.attributes
        accessTags = submission.accessTags
        extendedSection = ExtendedSection(submission.section)
    }

    var relPath = EMPTY
    var released = false
    var secretKey = EMPTY
    var version = 1
    var extendedSection = ExtendedSection(section)
    var processingStatus = PROCESSING
    var releaseTime: OffsetDateTime = OffsetDateTime.now()
    var modificationTime: OffsetDateTime = OffsetDateTime.now()
    var creationTime: OffsetDateTime = OffsetDateTime.now()

    fun asSubmission() = Submission(accNo, extendedSection.asSection(), attributes = subAttributes())

    private fun subAttributes(): List<Attribute> =
        attributes.plus(Attribute(SubFields.RELEASE_DATE, releaseTime.toLocalDate()))

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
