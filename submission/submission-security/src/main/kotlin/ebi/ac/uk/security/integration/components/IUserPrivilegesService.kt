package ebi.ac.uk.security.integration.components

import ac.uk.ebi.biostd.persistence.common.model.AccessType

interface IUserPrivilegesService {
    suspend fun canProvideAccNo(
        submitter: String,
        collection: String,
    ): Boolean

    fun canSubmitCollections(email: String): Boolean

    fun allowedCollections(
        email: String,
        accessType: AccessType,
    ): List<String>

    suspend fun canResubmit(
        submitter: String,
        accNo: String,
    ): Boolean

    suspend fun canSubmitToCollection(
        submitter: String,
        collection: String,
    ): Boolean

    suspend fun canDelete(
        submitter: String,
        accNo: String,
    ): Boolean

    suspend fun canDeleteFiles(
        submitter: String,
        accNo: String,
    ): Boolean

    fun canSubmitExtended(submitter: String): Boolean

    fun canRelease(email: String): Boolean

    /**
     * Permission to indicate the given user is able to release in the past, make a submission private or change a
     * public study release date.
     */
    fun canUpdateReleaseDate(
        email: String,
        collection: String?,
    ): Boolean
}
