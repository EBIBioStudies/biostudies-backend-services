package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.ATTACH_TO
import ebi.ac.uk.model.constants.SubFields.COLLECTION_VALIDATOR
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.model.constants.SubFields.RELEASE_DATE
import ebi.ac.uk.model.constants.SubFields.ROOT_PATH
import ebi.ac.uk.model.constants.SubFields.TITLE

/**
 * Return a simple submission which does not contain submission secret information.
 */
fun ExtSubmission.toSimpleSubmission(): Submission = Submission(
    accNo = accNo,
    section = section.toSection(),
    attributes = getSubmissionAttributes(),
    tags = tags.mapTo(mutableListOf()) { Pair(it.name, it.value) }
)

private fun ExtSubmission.getSubmissionAttributes(): List<Attribute> {
    val subAttrs =
        attributes
            .filterNot { it.name == COLLECTION_VALIDATOR.value }
            .map { it.toAttribute() }.toMutableSet()

    title?.let { subAttrs.add(Attribute(TITLE, it)) }
    releaseTime?.let { subAttrs.add(Attribute(RELEASE_DATE, it.toLocalDate())) }
    rootPath?.let { subAttrs.add(Attribute(ROOT_PATH, it)) }
    collections.filter { it.accNo != PUBLIC_ACCESS_TAG.value }.forEach { subAttrs.add(Attribute(ATTACH_TO, it.accNo)) }

    return subAttrs.toList()
}
