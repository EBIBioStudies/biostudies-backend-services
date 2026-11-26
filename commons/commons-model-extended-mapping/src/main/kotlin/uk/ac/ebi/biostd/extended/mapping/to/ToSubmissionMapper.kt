package uk.ac.ebi.biostd.extended.mapping.to

import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.model.constants.SubFields.ATTACH_TO
import uk.ac.ebi.biostd.model.constants.SubFields.COLLECTION_VALIDATOR
import uk.ac.ebi.biostd.model.constants.SubFields.DOI
import uk.ac.ebi.biostd.model.constants.SubFields.PUBLIC_ACCESS_TAG
import uk.ac.ebi.biostd.model.constants.SubFields.RELEASE_DATE
import uk.ac.ebi.biostd.model.constants.SubFields.ROOT_PATH
import uk.ac.ebi.biostd.model.constants.SubFields.TITLE

class ToSubmissionMapper(
    private val toSectionMapper: ToSectionMapper,
) {
    /**
     * Return a simple submission which does not contain submission extended information.
     */
    suspend fun toSimpleSubmission(
        sub: ExtSubmission,
        anonymize: Boolean = false,
    ): Submission {
        val simpleSubmission =
            Submission(
                accNo = sub.accNo,
                section = toSectionMapper.convert(sub.section, anonymize),
                attributes = sub.simpleAttributes(),
                tags = sub.tags.mapTo(mutableListOf()) { Pair(it.name, it.value) },
            )
        return simpleSubmission
    }

    private fun ExtSubmission.simpleAttributes(): List<Attribute> =
        buildSet {
            addAll(attributes.filter { it.name != COLLECTION_VALIDATOR.value }.map { it.toAttribute() })
            title?.let { add(Attribute(TITLE.value, it)) }
            doi?.let { add(Attribute(DOI.value, it)) }
            add(Attribute(RELEASE_DATE.value, releaseTime.toLocalDate().toString()))
            rootPath?.let { add(Attribute(ROOT_PATH.value, it)) }
            addAll(
                collections
                    .filter { it.accNo != PUBLIC_ACCESS_TAG.value }
                    .map { Attribute(ATTACH_TO.value, it.accNo) },
            )
        }.toList()

    companion object {
        const val DOUBLE_BLIND = "DoubleBlind"
    }
}
