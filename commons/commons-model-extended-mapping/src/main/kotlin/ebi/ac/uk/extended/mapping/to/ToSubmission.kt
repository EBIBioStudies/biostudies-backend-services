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
    attributes = getSubmissionAttributes(this),
    tags = tags.mapTo(mutableListOf()) { Pair(it.name, it.value) }
)

private fun getSubmissionAttributes(sub: ExtSubmission): List<Attribute> {
    val attrs = sub.attributes.filter { it.name != COLLECTION_VALIDATOR.value }.map { it.toAttribute() }.toMutableSet()

    sub.title?.let { attrs.add(Attribute(TITLE, it)) }
    sub.releaseTime?.let { attrs.add(Attribute(RELEASE_DATE, it.toLocalDate())) }
    sub.rootPath?.let { attrs.add(Attribute(ROOT_PATH, it)) }
    sub.collections.filter { it.accNo != PUBLIC_ACCESS_TAG.value }.forEach { attrs.add(Attribute(ATTACH_TO, it.accNo)) }

    return attrs.toList()
}
