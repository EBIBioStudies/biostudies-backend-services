package ebi.ac.uk.security.integration.components

interface IUserPrivilegesService {
    fun canProvideAccNo(email: String): Boolean

    fun canSubmitProjects(email: String): Boolean

    fun canResubmit(email: String, author: String, project: String?, accessTags: List<String>): Boolean

    fun canDelete(email: String, author: String, accessTags: List<String>): Boolean
}
