package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.integration.SubmissionDb
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields
import org.springframework.stereotype.Component

@Component
class SubmissionMapper(
        private val attributesMapper: AttributesMapper = AttributesMapper(),
        private val sectionMapper: SectionMapper = SectionMapper(attributesMapper, TabularMapper(attributesMapper))) {

    fun mapSubmission(submissionDb: SubmissionDb): Submission {
        return Submission(attributes = attributesMapper.toAttributes(submissionDb.attributes)).apply {
            accNo = submissionDb.accNo
            accessTags = getAccessTags(submissionDb.accessTags)
            rootSection = sectionMapper.toSection(submissionDb.rootSection)

            this[SubFields.ROOT_PATH] = submissionDb.rootPath.orEmpty()
            this[SubFields.TITLE] = submissionDb.title
            this[SubFields.RELEASE_TIME] = submissionDb.releaseTime
        }
    }

    private fun getAccessTags(accessTag: Set<AccessTag>): MutableList<String> {
        return accessTag.mapTo(mutableListOf(), AccessTag::name)
    }
}
