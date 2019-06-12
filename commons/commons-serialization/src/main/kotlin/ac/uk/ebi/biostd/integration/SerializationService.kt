package ac.uk.ebi.biostd.integration

import ebi.ac.uk.io.FilesSource
import ebi.ac.uk.model.Submission

interface SerializationService {
    fun deserializeSubmission(content: String, format: SubFormat): Submission

    fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission

    fun serializeSubmission(submission: Submission, format: SubFormat): String

    fun <T> serializeElement(element: T, format: SubFormat): String
}
