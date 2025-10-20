package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitToCollectionException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST}"
const val PATH_DIGITS = 3

class AccNoService(
    private val service: PersistenceService,
    private val patternUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService,
    private val queryService: SubmissionMetaQueryService,
    private val subBasePath: String?,
) {
    suspend fun calculateAccNo(
        attachTo: String?,
        user: String,
    ): AccNumber {
        checkCanSubmitToCollection(attachTo, user)
        val collection = attachTo?.let { queryService.getBasicCollection(it) }
        return calculateAccNo(getPattern(collection?.accNoPattern))
    }

    suspend fun checkAccess(
        accNo: String,
        user: String,
        attachTo: String?,
    ) {
        val exists = queryService.existByAccNo(accNo)
        when {
            exists -> checkCanReSubmit(user, accNo)
            else -> checkCanProvideAcc(user, attachTo)
        }
    }

    fun getRelPath(accNo: String): String {
        val accNumber = patternUtil.toAccNumber(accNo)
        return getRelPath(accNumber)
    }

    @Suppress("MagicNumber")
    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.prefix
        val suffix = accNo.numericValue.orEmpty().padStart(3, '0')
        val basePath = subBasePath?.trim('/')
        val basicRelPath = "$prefix/${suffix.takeLast(PATH_DIGITS)}/$accNo".removePrefix("/")
        return if (basePath != null) "$basePath/$basicRelPath" else basicRelPath
    }

    private suspend fun checkCanSubmitToCollection(
        collection: String?,
        submitter: String,
    ) {
        if (collection != null && privilegesService.canSubmitToCollection(submitter, collection).not()) {
            throw UserCanNotSubmitToCollectionException(submitter, collection)
        }
    }

    private suspend fun checkCanReSubmit(
        submitter: String,
        accNo: String,
    ) {
        if (privilegesService.canResubmit(submitter, accNo).not()) throw UserCanNotUpdateSubmit(accNo, submitter)
    }

    private suspend fun checkCanProvideAcc(
        submitter: String,
        collection: String?,
    ) {
        if (privilegesService.canProvideAccNo(submitter, collection.orEmpty()).not()) {
            throw UserCanNotProvideAccessNumber(submitter)
        }
    }

    private fun calculateAccNo(pattern: String): AccNumber = AccNumber(pattern, service.getSequenceNextValue(pattern).toString())

    private fun getPattern(parentPattern: String?) =
        when (parentPattern) {
            null -> patternUtil.getPattern(DEFAULT_PATTERN)
            else -> patternUtil.getPattern(parentPattern)
        }
}
