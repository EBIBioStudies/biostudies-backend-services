package ac.uk.ebi.biostd.client.common

import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap
import uk.ac.ebi.biostd.api.SubmitParameters
import uk.ac.ebi.biostd.api.SubmitParameters.Companion.SILENT_MODE
import uk.ac.ebi.biostd.api.SubmitParameters.Companion.SINGLE_JOB_MODE
import uk.ac.ebi.biostd.api.SubmitParameters.Companion.STORAGE_MODE
import uk.ac.ebi.biostd.model.constants.ATTRIBUTES
import uk.ac.ebi.biostd.model.constants.FILES
import uk.ac.ebi.biostd.model.constants.PREFERRED_SOURCES
import uk.ac.ebi.biostd.model.constants.SUBFORMAT
import uk.ac.ebi.biostd.model.constants.SUBMISSION
import uk.ac.ebi.biostd.model.constants.SUBMISSIONS
import java.io.File

internal fun multipartBody(
    submission: Any,
    parameters: SubmitParameters,
    files: List<File> = emptyList(),
): LinkedMultiValueMap<String, Any> =
    MultipartBuilder()
        .add(SUBMISSION, submission)
        .add(STORAGE_MODE, parameters.storageMode?.value)
        .add(SILENT_MODE, parameters.silentMode)
        .add(SINGLE_JOB_MODE, parameters.singleJobMode)
        .addAll(PREFERRED_SOURCES, parameters.preferredSources.map { it.name })
        .addAll(ATTRIBUTES, parameters.attributes)
        .addAll(FILES, files.map { FileSystemResource(it) })
        .build()

internal fun multipartBody(
    submissions: Map<String, String>,
    parameters: SubmitParameters,
    files: Map<String, List<File>>,
    format: String,
): LinkedMultiValueMap<String, Any> {
    val params =
        MultipartBuilder()
            .add(SUBMISSIONS, submissions)
            .add(STORAGE_MODE, parameters.storageMode?.value)
            .add(SILENT_MODE, parameters.silentMode)
            .add(SINGLE_JOB_MODE, parameters.singleJobMode)
            .add(SUBFORMAT, format)
            .addAll(PREFERRED_SOURCES, parameters.preferredSources.map { it.name })
            .addAll(ATTRIBUTES, parameters.attributes)
    files.forEach { (name, files) -> params.addAll(name, files.map { FileSystemResource(it) }) }
    return params.build()
}
