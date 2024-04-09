package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.ATTACH_TO
import ebi.ac.uk.model.constants.SubFields.COLLECTION_VALIDATOR
import ebi.ac.uk.model.constants.SubFields.DOI
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.model.constants.SubFields.RELEASE_DATE
import ebi.ac.uk.model.constants.SubFields.ROOT_PATH
import ebi.ac.uk.model.constants.SubFields.TITLE

class ToSubmissionMapper(private val toSectionMapper: ToSectionMapper) {
    /**
     * Return a simple submission which does not contain submission secret information. Used to generate public
     * submission tab files.
     */
    suspend fun toSimpleSubmission(sub: ExtSubmission): Submission =
        Submission(
            accNo = sub.accNo,
            section = toSectionMapper.convert(sub.section),
            attributes = sub.simpleAttributes(),
            tags = sub.tags.mapTo(mutableListOf()) { Pair(it.name, it.value) },
        )

    private fun ExtSubmission.simpleAttributes(): List<Attribute> =
        buildSet {
            addAll(attributes.filter { it.name != COLLECTION_VALIDATOR.value }.map { it.toAttribute() })
            title?.let { add(Attribute(TITLE.value, it)) }
            doi?.let { add(Attribute(DOI.value, it)) }
            releaseTime?.let { add(Attribute(RELEASE_DATE.value, it.toLocalDate().toString())) }
            rootPath?.let { add(Attribute(ROOT_PATH.value, it)) }
            addAll(collections.filter { it.accNo != PUBLIC_ACCESS_TAG.value }.map { Attribute(ATTACH_TO.value, it.accNo) })
        }.toList()
}
