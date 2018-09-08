package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.mapping.SubmissionMapper
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRepository
import ac.uk.ebi.biostd.serialization.json.JsonSerializer
import ac.uk.ebi.biostd.serialization.tsv.TsvSerializer
import ac.uk.ebi.biostd.serialization.xml.XmlSerializer

class SubmissionService(
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
}
