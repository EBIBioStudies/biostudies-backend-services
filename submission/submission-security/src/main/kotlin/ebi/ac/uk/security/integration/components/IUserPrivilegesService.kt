package ebi.ac.uk.security.integration.components

interface IUserPrivilegesService {
    fun canProvideAccNo(email: String): Boolean

    fun canSubmitProjects(email: String): Boolean

    fun canResubmit(submitter: String, author: String, tags: List<String>): Boolean

    fun canSubmitToProject(submitter: String, project: String): Boolean

    fun canDelete(submitter: String, author: String, accessTags: List<String>): Boolean
}
