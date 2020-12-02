package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.ProvideAccessNumber
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitToProjectException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

const val DEFAULT_PATTERN = "!{S-BSST}"
const val PATH_DIGITS = 3

class AccNoService(
    private val service: PersistenceService,
    private val patternUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService
) {
    @Suppress("ThrowsCount")
    fun calculateAccNo(request: AccNoServiceRequest): AccNumber {
        val (submitter, accNo, isNew, project, projectPattern) = request
        checkCanProvideAcc(accNo, submitter)
        checkCanSubmitToProject(project, submitter)

        if (accNo != null && isNew.not()) {
            checkCanReSubmit(accNo, submitter)
            return patternUtil.toAccNumber(accNo)
        }

        return accNo?.let { patternUtil.toAccNumber(it) } ?: calculateAccNo(getPattern(projectPattern))
    }

    private fun checkCanSubmitToProject(project: String?, submitter: String) {
        if (project != null && privilegesService.canSubmitToProject(submitter, project).not())
            throw UserCanNotSubmitToProjectException(submitter, project)
    }

    private fun checkCanReSubmit(accNo: String, submitter: String) {
        if (privilegesService.canResubmit(submitter, accNo).not()) throw UserCanNotUpdateSubmit(accNo, submitter)
    }

    private fun checkCanProvideAcc(accNo: String?, submitter: String) {
        if (accNo != null && privilegesService.canProvideAccNo(submitter).not()) throw ProvideAccessNumber(submitter)
    }

    private fun calculateAccNo(pattern: String) = AccNumber(pattern, service.getSequenceNextValue(pattern).toString())

    @Suppress("MagicNumber")
    internal fun getRelPath(accNo: AccNumber): String {
        val prefix = accNo.prefix
        val suffix = accNo.numericValue.orEmpty().padStart(3, '0')
        return "$prefix/${suffix.takeLast(PATH_DIGITS)}/$accNo".removePrefix("/")
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
    val projectPattern: String? = null
)
