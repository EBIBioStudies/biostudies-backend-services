package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitToCollectionException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST}"
const val PATH_DIGITS = 3

class AccNoService(
    private val service: PersistenceService,
    private val patternUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService,
    private val subBasePath: String?,
) {
    suspend fun calculateAccNo(
        submitter: String,
        submission: Submission,
        collection: BasicCollection?,
        previousVersion: ExtSubmission?,
    ): Pair<String, String> {
        val accNumber = getAccNo(submitter, submission, collection, previousVersion)
        val relPath = getRelPath(accNumber)

        return (accNumber.toString() to relPath)
    }

    @Suppress("ThrowsCount")
    private suspend fun getAccNo(
        submitter: String,
        submission: Submission,
        collection: BasicCollection?,
        previousVersion: ExtSubmission?,
    ): AccNumber {
        val isNew = previousVersion == null
        val accNo = submission.accNo.ifBlank { null }
        val collectionAccNo = collection?.accNo

        if (accNo != null && isNew.not()) {
            checkCanReSubmit(accNo, submitter)
            return patternUtil.toAccNumber(accNo)
        }

        checkCanSubmitToCollection(collectionAccNo, submitter)
        checkCanProvideAcc(accNo, collectionAccNo, isNew, submitter)
        return accNo?.let { patternUtil.toAccNumber(it) } ?: calculateAccNo(getPattern(collection?.accNoPattern))
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
        accNo: String,
        submitter: String,
    ) {
        if (privilegesService.canResubmit(submitter, accNo).not()) throw UserCanNotUpdateSubmit(accNo, submitter)
    }

    private suspend fun checkCanProvideAcc(
        accNo: String?,
        collection: String?,
        isNew: Boolean,
        submitter: String,
    ) {
        if (isNew && accNo != null && privilegesService.canProvideAccNo(submitter, collection.orEmpty()).not()) {
            throw UserCanNotProvideAccessNumber(submitter)
        }
    }

    private fun calculateAccNo(pattern: String) = AccNumber(pattern, service.getSequenceNextValue(pattern).toString())

    @Suppress("MagicNumber")
    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.prefix
        val suffix = accNo.numericValue.orEmpty().padStart(3, '0')
        val basePath = subBasePath?.trim('/')
        val basicRelPath = "$prefix/${suffix.takeLast(PATH_DIGITS)}/$accNo".removePrefix("/")
        return if (basePath != null) "$basePath/$basicRelPath" else basicRelPath
    }

    private fun getPattern(parentPattern: String?) =
        when (parentPattern) {
            null -> patternUtil.getPattern(DEFAULT_PATTERN)
            else -> patternUtil.getPattern(parentPattern)
        }
}
