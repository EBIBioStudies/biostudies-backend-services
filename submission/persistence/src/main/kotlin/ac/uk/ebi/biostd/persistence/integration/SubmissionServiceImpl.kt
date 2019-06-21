package ac.uk.ebi.biostd.persistence.integration

import ac.uk.ebi.biostd.persistence.mapping.ext.from.ExtToDbMapper
import ac.uk.ebi.biostd.persistence.mapping.ext.to.toExtSubmission
import ac.uk.ebi.biostd.persistence.repositories.LockExecutor
import ac.uk.ebi.biostd.persistence.repositories.SequenceDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.service.SubFileResolver
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

internal class SubmissionServiceImpl(
    private val subRepository: SubmissionDataRepository,
    private val sequenceRepository: SequenceDataRepository,
    private val lockExecutor: LockExecutor,
    private val subFileResolver: SubFileResolver,
    private val mapper: ExtToDbMapper,
    private val submitter: Submitter
) : SubmissionService {
    override fun canDelete(accNo: String, asUser: User): Boolean {
        return true
    }

    override fun submit(extSubmission: ExtSubmission, user: User): ExtSubmission {
        submitter.submitSubmission(extSubmission)
        return saveSubmission(extSubmission, user)
    }

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
        TODO("not implemented")
    }

    override fun existProject(accNo: String): Boolean {
        TODO("not implemented")
    }

    override fun canUserProvideAccNo(user: User) = true

    override fun canSubmit(accNo: String, user: User) = true

    override fun canAttach(accNo: String, user: User): Boolean = true

    private fun saveSubmission(extSubmission: ExtSubmission, user: User): ExtSubmission {
        val submissionDb = mapper.toSubmissionDb(extSubmission, user)
        return lockExecutor.executeLocking(submissionDb.accNo) {
            val nextVersion = (subRepository.getLastVersion(extSubmission.accNo) ?: 0) + 1
            subRepository.expireActiveVersions(extSubmission.accNo)
            submissionDb.version = nextVersion
            subRepository.save(submissionDb)
            submissionDb.toExtSubmission(subFileResolver.getSubmissionSource(submissionDb.secretKey))
        }
    }
}
