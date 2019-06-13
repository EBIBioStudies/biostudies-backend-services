package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import arrow.core.Option
import arrow.core.toOption
import ebi.ac.uk.base.toOption
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.functions.secondsToInstant
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import ebi.ac.uk.model.constants.SubFields.ATTACH_TO
import java.time.OffsetDateTime
import java.time.ZoneOffset

class SubmissionServiceImpl(
    private val subRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val lockExecutor: LockExecutor,
    private val exte
) : SubmissionService {


    override fun isNew(accNo: String): Boolean = subRepository.existsByAccNo(accNo)

    override fun getSequenceNextValue(pattern: AccPattern): Long {
        val sequence = sequenceRepository.getByPrefixAndSuffix(pattern.prefix, pattern.postfix)
        sequence.counter = sequence.counter + 1
        sequenceRepository.save(sequence)
        return sequence.id
    }

    override fun getCreationTime(accNo: String): OffsetDateTime =
        secondsToInstant(subRepository.getByAccNo(accNo).creationTime).atOffset(ZoneOffset.UTC)

    override fun getProjectAccPattern(submission: Submission): Option<String> =
        getParentSubmission(submission)
            .flatMap { parent -> parent.attributes.firstOrNull { it.name == ACC_NO_TEMPLATE.toString() }.toOption() }
            .map { it.value }

    private fun getParentSubmission(submission: Submission): Option<ac.uk.ebi.biostd.persistence.model.Submission> =
        submission.find(ATTACH_TO).toOption().map { subRepository.getByAccNoAndVersionGreaterThan(it) }

    override fun getProjectAccessTags(accNo: String): List<String> {

    }

    override fun existProject(accNo: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canUserProvideAccNo(user: User) = true

    override fun canSubmit(accNo: String, user: User) = true

    override fun canAttach(accNo: String): Boolean = true

    override fun saveSubmission(submission: ExtSubmission, user: User): ExtSubmission {
        lockExecutor.executeLocking(submission.accNo) {
            val nextVersion = (subRepository.getLastVersion(submission.accNo) ?: 0) + 1
            subRepository.expireActiveVersions(submission.accNo)
            submission.version = nextVersion
            subRepository.save(subMapper.toSubmissionDb(submission))
        }
    }

}
