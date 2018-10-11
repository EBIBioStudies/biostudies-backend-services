package ac.uk.ebi.biostd.mapping

import ac.uk.ebi.biostd.submission.Submission
import ebi.ac.uk.model.IAccessTag
import ebi.ac.uk.model.ISubmission

class SubmissionMapper(
        private val attributesMapper: AttributesMapper,
        private val sectionMapper: SectionMapper) {

    fun mapSubmission(submission: ISubmission): Submission {
        return Submission().apply {
            accNo = submission.accNo
            accessTags = getAccessTags(submission.accessTags)
            rootPath = submission.rootPath
            title = submission.title
            rtime = submission.releaseTime.toEpochSecond()
            attributes = attributesMapper.toAttributes(submission.attributes)
            section = sectionMapper.toSection(submission.rootSection)
        }
    }

    private fun getAccessTags(accessTag: Set<IAccessTag>) = accessTag.mapTo(mutableListOf(), IAccessTag::name)

}
