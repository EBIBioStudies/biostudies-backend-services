package ac.uk.ebi.biostd.client.common

import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.api.SubmitParameters.Companion.SILENT_MODE
import ebi.ac.uk.api.SubmitParameters.Companion.SINGLE_JOB_MODE
import ebi.ac.uk.api.SubmitParameters.Companion.STORAGE_MODE
import ebi.ac.uk.commons.http.builder.linkedMultiValueMapOf
import ebi.ac.uk.model.constants.ATTRIBUTES
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.PREFERRED_SOURCES
import ebi.ac.uk.model.constants.SUBMISSION
import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap
import java.io.File

internal fun multipartBody(
    submission: Any,
    parameters: SubmitParameters,
    files: List<File> = emptyList(),
): LinkedMultiValueMap<String, Any> {
    val (sources, attributes, storageMode, silentMode, singleJobMode) = parameters
    val pairs =
        buildList<Pair<String, Any>> {
            add(SUBMISSION to submission)

            addAll(sources.orEmpty().map { PREFERRED_SOURCES to it.name })
            addAll(attributes.orEmpty().map { ATTRIBUTES to it })
            storageMode?.let { add(STORAGE_MODE to it.value) }
            silentMode?.let { add(SILENT_MODE to it) }
            singleJobMode?.let { add(SINGLE_JOB_MODE to it) }

            addAll(files.map { FILES to FileSystemResource(it) })
        }
    return linkedMultiValueMapOf(pairs)
}
