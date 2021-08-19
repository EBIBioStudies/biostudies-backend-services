package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.SourcesRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class ExtSubmissionsWebHandler(
    private val extSubmissionService: ExtSubmissionService,
    private val webHandlerHelper: SubmissionsWebHandlerHelper,
    private val extSerializationService: ExtSerializationService
) {
    fun submit(request: ContentSubmitWebRequest): ExtSubmission {
        val extSub = extSerializationService.deserialize<ExtSubmission>(request.submission)
        val sourcesRequest = SourcesRequest(extSub.accNo, extSub.rootPath, request.submitter, request.files)
        val sources = webHandlerHelper.submissionSources(sourcesRequest)
        val fileLists = extSub
            .allFileList
            .map { sources.fileSources.getFile(it.fileName) }

        return extSubmissionService.submitExtendedSubmission(request.submitter.email, extSub, fileLists)
    }
}
