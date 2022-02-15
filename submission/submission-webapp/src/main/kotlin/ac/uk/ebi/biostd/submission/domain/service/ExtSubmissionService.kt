package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.events.SubmissionRequestMessage
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.FileMode.MOVE
import ebi.ac.uk.extended.model.isCollection
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import mu.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import uk.ac.ebi.events.config.BIOSTUDIES_EXCHANGE
import uk.ac.ebi.events.config.SUBMISSIONS_PARTIAL_UPDATE_ROUTING_KEY
import uk.ac.ebi.events.config.SUBMISSIONS_REQUEST_ROUTING_KEY
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions", "LongParameterList")
class ExtSubmissionService(
    private val rabbitTemplate: RabbitTemplate,
    private val submissionSubmitter: SubmissionSubmitter,
    private val submissionQueryService: SubmissionQueryService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val securityQueryService: ISecurityQueryService,
    private val extSerializationService: ExtSerializationService,
    private val eventsPublisherService: EventsPublisherService
) {
    fun getExtendedSubmission(accNo: String): ExtSubmission = submissionQueryService.getExtByAccNo(accNo)

    fun findExtendedSubmission(accNo: String): ExtSubmission? = submissionQueryService.findExtByAccNo(accNo)

    fun getReferencedFiles(
        accNo: String,
        fileListName: String
    ): ExtFileTable = ExtFileTable(submissionQueryService.getReferencedFiles(accNo, fileListName))

    fun refreshSubmission(accNo: String, user: String): ExtSubmission {
        val submission = submissionQueryService.getExtByAccNo(accNo, includeFileListFiles = true)
        val refreshedSubmission = submitExt(user, submission, emptyList(), MOVE)
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE,
            SUBMISSIONS_PARTIAL_UPDATE_ROUTING_KEY,
            eventsPublisherService.submissionMessage(refreshedSubmission.accNo, refreshedSubmission.owner)
        )

        return refreshedSubmission
    }

    fun reTriggerSubmission(accNo: String, version: Int): ExtSubmission {
        return submissionSubmitter.processRequest(accNo, version)
    }

    fun submitExt(
        user: String,
        extSubmission: ExtSubmission,
        fileListFiles: List<File> = emptyList(),
        fileMode: FileMode = COPY
    ): ExtSubmission {
        val submission = processExtSubmission(user, extSubmission, fileListFiles)
        val (accNo, version) = submissionSubmitter.submitAsync(SubmissionRequest(submission, fileMode))
        return submissionSubmitter.processRequest(accNo, version)
    }

    fun submitExtAsync(
        user: String,
        sub: ExtSubmission,
        fileListFiles: List<File> = emptyList(),
        fileMode: FileMode
    ) {
        logger.info { "${sub.accNo} $user Received async submit request for ext submission ${sub.accNo}" }
        val submission = processExtSubmission(user, sub, fileListFiles)
        val (accNo, version) = submissionSubmitter.submitAsync(SubmissionRequest(submission, fileMode))
        rabbitTemplate.convertAndSend(
            BIOSTUDIES_EXCHANGE,
            SUBMISSIONS_REQUEST_ROUTING_KEY,
            SubmissionRequestMessage(accNo, version)
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

        val page = submissionQueryService.getExtendedSubmissions(filter)
        return PageImpl(page.content, page.pageable, page.totalElements)
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

    private fun deserializeFiles(fileList: File): List<ExtFile> =
        fileList.inputStream().use { extSerializationService.deserialize(it).toList() }

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

    private fun validateCollection(accNo: String) = require(submissionQueryService.existByAccNo(accNo)) {
        throw CollectionNotFoundException(accNo)
    }
}
