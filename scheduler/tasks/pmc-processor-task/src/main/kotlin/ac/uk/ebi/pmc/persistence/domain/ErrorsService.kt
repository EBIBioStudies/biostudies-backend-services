package ac.uk.ebi.pmc.persistence.domain

import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument
import ac.uk.ebi.pmc.persistence.repository.ErrorsDataRepository
import ac.uk.ebi.scheduler.properties.PmcMode
import java.io.PrintWriter
import java.io.StringWriter

class ErrorsService(
    private val repository: ErrorsDataRepository,
    private val submissionService: SubmissionService,
) {
    suspend fun saveError(
        sourceFile: String,
        submissionText: String,
        mode: PmcMode,
        it: Throwable,
    ) {
        val doc =
            SubmissionErrorDocument(
                sourceFile = sourceFile,
                submissionText = submissionText,
                mode = mode,
                error = getStackTrace(it),
            )
        repository.save(doc)
    }

    suspend fun saveError(
        sub: SubmissionDocument,
        mode: PmcMode,
        error: Throwable,
    ) {
        val doc =
            SubmissionErrorDocument(
                accNo = sub.accNo,
                sourceFile = sub.sourceFile,
                submissionText = sub.body,
                mode = mode,
                error = getStackTrace(error),
            )
        submissionService.reportError(sub, mode)
        repository.save(doc)
    }

    private fun getStackTrace(throwable: Throwable): String {
        var sw = StringWriter()
        var pw = PrintWriter(sw, true)
        throwable.printStackTrace(pw)
        return sw.buffer.toString()
    }
}
