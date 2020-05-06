package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.creationTime
import ebi.ac.uk.model.extensions.modificationTime
import ebi.ac.uk.model.extensions.releaseTime
import ebi.ac.uk.model.extensions.secretKey

/**
 * Return a simple submission which does not contain submission secret information.
 */
fun ExtSubmission.toSimpleSubmission(): Submission {
    return Submission(
        accNo = accNo,
        section = section.toSection(),
        attributes = getSubmissionAttributes(),
        tags = tags.mapTo(mutableListOf()) { Pair(it.name, it.value) }
    )
}

// TODO: finally fix attributes by difenning which are and which not
private fun ExtSubmission.getSubmissionAttributes(): List<Attribute> {
    val subAttrs = attributes.map { it.toAttribute() }.toMutableSet()
    releaseTime?.let { subAttrs.add(Attribute(SubFields.RELEASE_DATE, it.toLocalDate())) }
    title?.let { subAttrs.add(Attribute(SubFields.TITLE, it)) }
    return subAttrs.toList()
}

/**
 * Return a extended submission which contains submission secret information and internal information.
 */
fun ExtSubmission.toExtSubmission(): Submission {
    val submission = Submission(
        accNo = accNo,
        section = section.toSection(),
        attributes = attributes.map { it.toAttribute() }.toMutableList(),
        tags = tags.mapTo(mutableListOf()) { Pair(it.name, it.value) },
        accessTags = accessTags.mapTo(mutableListOf()) { it.name }
    )

    submission.secretKey = secretKey
    submission.creationTime = creationTime
    submission.modificationTime = modificationTime
    submission.releaseTime = releaseTime
    return submission
}
