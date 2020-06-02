package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.constants.SubFields.ATTACH_TO
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
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

private fun ExtSubmission.getSubmissionAttributes(): List<Attribute> {
    val subAttrs = attributes.map { it.toAttribute() }.toMutableSet()
    title?.let { subAttrs.add(Attribute(SubFields.TITLE, it)) }
    releaseTime?.let { subAttrs.add(Attribute(SubFields.RELEASE_DATE, it.toLocalDate())) }
    rootPath?.let { subAttrs.add(Attribute(SubFields.ROOT_PATH, it)) }
    accessTags.filter { it.name != PUBLIC_ACCESS_TAG.value }.forEach { subAttrs.add(Attribute(ATTACH_TO, it.name)) }
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
        tags = tags.mapTo(mutableListOf()) { Pair(it.name, it.value) }
    )

    submission.secretKey = secretKey
    submission.creationTime = creationTime
    submission.modificationTime = modificationTime
    submission.releaseTime = releaseTime
    return submission
}
