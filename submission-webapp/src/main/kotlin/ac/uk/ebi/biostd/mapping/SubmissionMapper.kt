package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.config.SubmissionDb
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.submission.Submission
import org.springframework.stereotype.Component


@Component
class SubmissionMapper(
        private val attributesMapper: AttributesMapper,
        private val sectionMapper: SectionMapper) {

    fun mapSubmission(submissionDb: SubmissionDb): Submission {
        return Submission().apply {
            accNo = submissionDb.accNo
            accessTags = getAccessTags(submissionDb.accessTags)
            rootPath = submissionDb.rootPath
            title = submissionDb.title
            rTime = submissionDb.releaseTime
            attributes = attributesMapper.toAttributes(submissionDb.attributes)
            section = sectionMapper.toSection(submissionDb.rootSection)
        }
    }

    private fun getAccessTags(accessTag: Set<AccessTag>): MutableList<String> {
        return accessTag.map(AccessTag::name).toMutableList()
    }
}
