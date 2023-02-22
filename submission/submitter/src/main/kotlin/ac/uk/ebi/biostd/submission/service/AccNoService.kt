package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitToProjectException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST}"
const val PATH_DIGITS = 3

class AccNoService(
    private val service: PersistenceService,
    private val patternUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService,
    private val subBasePath: String,
) {
    @Suppress("ThrowsCount")
    fun calculateAccNo(request: AccNoServiceRequest): AccNumber {
        val (submitter, accNo, isNew, project, projectPattern) = request

        if (accNo != null && isNew.not()) {
            checkCanReSubmit(accNo, submitter)
            return patternUtil.toAccNumber(accNo)
        }

        checkCanSubmitToProject(project, submitter)
        checkCanProvideAcc(accNo, isNew, submitter)
        return accNo?.let { patternUtil.toAccNumber(it) } ?: calculateAccNo(getPattern(projectPattern))
    }

    private fun checkCanSubmitToProject(project: String?, submitter: String) {
        if (project != null && privilegesService.canSubmitToCollection(submitter, project).not())
            throw UserCanNotSubmitToProjectException(submitter, project)
    }

    private fun checkCanReSubmit(accNo: String, submitter: String) {
        if (privilegesService.canResubmit(submitter, accNo).not()) throw UserCanNotUpdateSubmit(accNo, submitter)
    }

    private fun checkCanProvideAcc(accNo: String?, isNew: Boolean, submitter: String) {
        if (isNew && accNo != null && privilegesService.canProvideAccNo(submitter).not())
            throw UserCanNotProvideAccessNumber(submitter)
    }

    private fun calculateAccNo(pattern: String) = AccNumber(pattern, service.getSequenceNextValue(pattern).toString())

    @Suppress("MagicNumber")
    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.prefix
        val suffix = accNo.numericValue.orEmpty().padStart(3, '0')
        val basePath = subBasePath.trim('/').nullIfBlank()
        val basicRelPath = "$prefix/${suffix.takeLast(PATH_DIGITS)}/$accNo".removePrefix("/")
        return if (basePath != null) "$basePath/$basicRelPath" else basicRelPath
    }

    private fun getPattern(parentPattern: String?) = when (parentPattern) {
        null -> patternUtil.getPattern(DEFAULT_PATTERN)
        else -> patternUtil.getPattern(parentPattern)
    }
}

data class AccNoServiceRequest(
    val submitter: String,
    val accNo: String? = null,
    val isNew: Boolean = true,
    val project: String? = null,
    val projectPattern: String? = null,
)
