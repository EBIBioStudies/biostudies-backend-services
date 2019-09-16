package ebi.ac.uk.security.integration.components

import ebi.ac.uk.model.User

interface IUserPrivilegesService {
    fun canProvideAccNo(email: String): Boolean

    fun canResubmit(email: String, author: User, project: String?, accessTags: List<String>): Boolean

    fun canDelete(accNo: String, email: String): Boolean
}
