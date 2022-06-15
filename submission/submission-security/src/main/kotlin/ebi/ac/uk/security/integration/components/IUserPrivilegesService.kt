package ebi.ac.uk.security.integration.components

import ac.uk.ebi.biostd.persistence.common.model.AccessType

interface IUserPrivilegesService {
    fun canProvideAccNo(email: String): Boolean

    fun canSubmitCollections(email: String): Boolean

    fun allowedCollections(email: String, accessType: AccessType): List<String>

    fun canResubmit(submitter: String, accNo: String): Boolean

    fun canSubmitToCollection(submitter: String, collection: String): Boolean

    fun canDelete(submitter: String, accNo: String): Boolean

    fun canSubmitExtended(submitter: String): Boolean

    fun canRelease(email: String): Boolean
}
