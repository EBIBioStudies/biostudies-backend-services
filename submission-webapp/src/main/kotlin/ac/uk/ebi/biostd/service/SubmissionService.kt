package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRepository
import ac.uk.ebi.biostd.submission.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import arrow.core.Option
import ebi.ac.uk.model.IAccessTag
import ebi.ac.uk.model.ISubmission
import ebi.ac.uk.model.submission.SubmitOperation

class SubmissionService(
        private val submissionSubmitter: SubmissionSubmitter,
        private val subRepository: SubmissionRepository,
        private val submissionMapper: SubmissionMapper,
        private val jsonSerializer: JsonSerializer,
        private val tsvSerializer: TsvSerializer,
        private val xmlSerializer: XmlSerializer) {

    fun getSubmissionAsJson(accNo: String): String {
        val submission = subRepository.findByAccNoAndVersionGreaterThan(accNo)
        return jsonSerializer.serialize(submissionMapper.mapSubmission(submission))
    }

    fun getSubmissionAsXml(accNo: String): String {
        val submission = subRepository.findByAccNoAndVersionGreaterThan(accNo)
        return xmlSerializer.serialize(submissionMapper.mapSubmission(submission))
    }

    fun getSubmissionAsTsv(accNo: String): String {
        val submission = subRepository.findByAccNoAndVersionGreaterThan(accNo)
        return tsvSerializer.serialize(submissionMapper.mapSubmission(submission))
    }

    fun submitSubmission(submission: ISubmission): ISubmission {
        return submissionSubmitter.submit(submission, SubmitOperation.CREATE, SubmissionService.Companion)
    }

    companion object : PersistenceContext {
        override fun getSequenceNextValue(name: String): Long {
            return 5000
        }

        override fun getParentAccessTags(submissionDb: ISubmission): List<IAccessTag> {
            return emptyList()
        }

        override fun getParentAccPattern(submissionDb: ISubmission): Option<String> {
            return Option.empty()
        }
    }
}
