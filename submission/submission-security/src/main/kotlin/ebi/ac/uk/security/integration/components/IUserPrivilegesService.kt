package ebi.ac.uk.security.integration.components

interface IUserPrivilegesService {
    fun canProvideAccNo(email: String): Boolean

    fun canSubmit(accNo: String, email: String): Boolean

    fun canDelete(accNo: String, email: String): Boolean
}
