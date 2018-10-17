package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.integration.SubmissionDb
import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.submission.Submission
import org.springframework.stereotype.Component


@Component
class SubmissionMapper(
        private val attributesMapper: AttributesMapper = AttributesMapper(),
        private val sectionMapper: SectionMapper = SectionMapper(attributesMapper, TabularMapper(attributesMapper))) {

    fun mapSubmission(submissionDb: SubmissionDb): Submission {
        return Submission().apply {
            accNo = submissionDb.accNo
            accessTags = getAccessTags(submissionDb.accessTags)
            rootPath = submissionDb.rootPath
            title = submissionDb.title
            rtime = submissionDb.releaseTime
            attributes = attributesMapper.toAttributes(submissionDb.attributes)
            section = sectionMapper.toSection(submissionDb.rootSection)
        }
    }

    private fun getAccessTags(accessTag: Set<AccessTag>): MutableList<String> {
        return accessTag.map(AccessTag::name).toMutableList()
    }
}
