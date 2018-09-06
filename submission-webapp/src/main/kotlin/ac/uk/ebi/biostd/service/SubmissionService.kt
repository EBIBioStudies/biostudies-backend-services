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

    fun getSubmissionAsJson(id: Long): String {
        val submission = subRepository.getOne(id)
        return jsonSerializer.serialize(submissionMapper.mapSubmission(submission))
    }

    fun getSubmissionAsXml(id: Long): String {
        val submission = subRepository.getOne(id)
        return xmlSerializer.serialize(submissionMapper.mapSubmission(submission))
    }

    fun getSubmissionAsTsv(id: Long): String {
        val submission = subRepository.getOne(id)
        return tsvSerializer.serialize(submissionMapper.mapSubmission(submission))
    }
}
