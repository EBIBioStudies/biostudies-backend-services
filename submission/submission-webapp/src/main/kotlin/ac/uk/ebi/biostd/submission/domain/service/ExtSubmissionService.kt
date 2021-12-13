package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.isCollection
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class ExtSubmissionService(
    private val rabbitTemplate: RabbitTemplate,
    private val submissionSubmitter: SubmissionSubmitter,
    private val submissionRepository: SubmissionQueryService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val securityQueryService: ISecurityQueryService,
    private val extSerializationService: ExtSerializationService
) {
    fun getExtendedSubmission(accNo: String): ExtSubmission = submissionRepository.getExtByAccNo(accNo)

    fun findExtendedSubmission(accNo: String): ExtSubmission? = submissionRepository.findExtByAccNo(accNo)

    fun getReferencedFiles(
        accNo: String,
        fileListName: String
    ): ExtFileTable = ExtFileTable(submissionRepository.getReferencedFiles(accNo, fileListName))

    fun submitExt(
        user: String,
        extSubmission: ExtSubmission,
        fileListFiles: List<File> = emptyList(),
        fileMode: FileMode = COPY
    ): ExtSubmission {
        val submission = processExtSubmission(user, extSubmission, fileListFiles)
        return submissionSubmitter.submit(SaveSubmissionRequest(submission, fileMode))
    }

    fun submitExtAsync(
        user: String,
        extSubmission: ExtSubmission,
        fileListFiles: List<File> = emptyList(),
        fileMode: FileMode
    ) {
        val accNo = extSubmission.accNo
        logger.info { "$accNo $user Received async submit request for ext submission $accNo" }

        val submission = processExtSubmission(user, extSubmission, fileListFiles)
        val newVersion = submissionSubmitter.submitAsync(SaveSubmissionRequest(submission, fileMode))

        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE,
            SUBMISSIONS_REQUEST_ROUTING_KEY,
            SubmissionRequestMessage(newVersion.accNo, newVersion.version, fileMode, newVersion.owner, null)
        )
    }

    fun getExtendedSubmissions(request: ExtPageRequest): Page<ExtSubmission> {
        val filter = SubmissionFilter(
            rTimeFrom = request.fromRTime?.let { OffsetDateTime.parse(request.fromRTime) },
            rTimeTo = request.toRTime?.let { OffsetDateTime.parse(request.toRTime) },
            released = request.released,
            limit = request.limit,
            offset = request.offset
        )

        val page = submissionRepository
            .getExtendedSubmissions(filter)
            .onEach { it.onFailure { logger.error { it.message ?: it.localizedMessage } } }
            .map { it.getOrNull() }
        val submissions = page.content.filterNotNull()

        return PageImpl(submissions, page.pageable, page.totalElements)
    }

    private fun processExtSubmission(
        user: String,
        extSubmission: ExtSubmission,
        fileListFiles: List<File>
    ): ExtSubmission {
        validateSubmitter(user)
        validateSubmission(extSubmission)

        return extSubmission.copy(
            submitter = user,
            section = processFileListFiles(extSubmission.section, fileListFiles.associateBy { it.nameWithoutExtension })
        )
    }

    private fun processFileListFiles(
        section: ExtSection,
        fileList: Map<String, File>
    ): ExtSection = section.copy(
        fileList = section.fileList?.let { it.copy(files = deserializeFiles(fileList.getValue(it.fileName))) },
        sections = section.sections.map { subSec -> subSec.bimap({ processFileListFiles(it, fileList) }, { it }) }
    )

    private fun deserializeFiles(fileList: File) =
        extSerializationService.deserialize(fileList.readText(), ExtFileTable::class.java).files

    private fun validateSubmission(submission: ExtSubmission) {
        validateOwner(submission.owner)
        if (submission.isCollection.not()) submission.collections.forEach { validateCollection(it.accNo) }
    }

    private fun validateSubmitter(user: String) = require(userPrivilegesService.canSubmitExtended(user)) {
        throw SecurityException("The user '$user' is not allowed to perform this action")
    }

    private fun validateOwner(email: String) = require(securityQueryService.existsByEmail(email, false)) {
        throw UserNotFoundException(email)
    }

    private fun validateCollection(accNo: String) = require(submissionRepository.existByAccNo(accNo)) {
        throw CollectionNotFoundException(accNo)
    }
}
