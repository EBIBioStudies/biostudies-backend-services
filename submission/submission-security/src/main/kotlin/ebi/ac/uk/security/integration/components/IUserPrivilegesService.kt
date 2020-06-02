package ebi.ac.uk.security.integration.components

import ac.uk.ebi.biostd.persistence.model.AccessType

interface IUserPrivilegesService {
    fun canProvideAccNo(email: String): Boolean

    fun canSubmitProjects(email: String): Boolean

    fun allowedProjects(email: String, accessType: AccessType): List<String>

    fun canResubmit(submitter: String, accNo: String): Boolean

    fun canSubmitToProject(submitter: String, project: String): Boolean

    fun canDelete(submitter: String, accNo: String): Boolean

    fun canSubmitExtended(submitter: String): Boolean
}
