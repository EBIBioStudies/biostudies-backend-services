package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicProject
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService

class SubmissionMongoMetaQueryService : SubmissionMetaQueryService {
    override fun getBasicProject(accNo: String): BasicProject {
        TODO("Not yet implemented")
    }

    override fun findLatestBasicByAccNo(accNo: String): BasicSubmission? {
        return null
    }

    override fun getAccessTags(accNo: String): List<String> {
        return emptyList()
    }

    override fun existByAccNo(accNo: String): Boolean {
        return false
    }
}
