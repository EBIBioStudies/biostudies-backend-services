package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ac.uk.ebi.biostd.submission.exceptions.ValidationException
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ac.uk.ebi.biostd.submission.validators.SubmissionValidator
import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus
import ebi.ac.uk.errors.ValidationTree
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.collections.ifNotEmpty
import java.lang.Exception

class SubmissionSubmitter(
    private val validators: List<SubmissionValidator>,
    private val processors: List<SubmissionProcessor>,
    private val filesHandler: FilesHandler
) {
    fun submit(
        submission: ExtendedSubmission,
        files: FilesSource,
        context: PersistenceContext
    ): Submission {


        var validationNodes = mutableListOf<ValidationNode>()

        validators.map {
            try {
                it.validate(submission, context)
            } catch (e: ValidationException) {
                ValidationNode(ValidationNodeStatus.ERROR, e.message ?: e.javaClass.name)
            }
        }.filterIsInstance<ValidationNode>().ifNotEmpty {
           validationNodes.add(ValidationNode(ValidationNodeStatus.ERROR, "Validation errors", it))
        }

        processors.map {
            try {
                it.process(submission, context)
            } catch (e: ValidationException) {
                ValidationNode(ValidationNodeStatus.ERROR, e.message ?: e.javaClass.name)
            }
        }.filterIsInstance<ValidationNode>().ifNotEmpty {
            validationNodes.add(ValidationNode(ValidationNodeStatus.ERROR, "Processing errors", it))
        }

        try {
            filesHandler.processFiles(submission, files)
        } catch (e: InvalidFilesException) {
            validationNodes.add(ValidationNode(ValidationNodeStatus.ERROR,
                e.message.plus(" ").plus(e.invalidFiles.joinToString { it.path })))
        }

        validationNodes.ifNotEmpty { throw ValidationException("Submission validation errors", validationNodes) }

        context.saveSubmission(submission)
        return submission
    }

}
