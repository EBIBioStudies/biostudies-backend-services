package ac.uk.ebi.biostd.client.common

import ebi.ac.uk.api.STORAGE_MODE
import ebi.ac.uk.api.SubmitParameters
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
    val (sources, attributes, storageMode) = parameters
    val pairs =
        buildList<Pair<String, Any>> {
            add(SUBMISSION to submission)

            addAll(sources.orEmpty().map { PREFERRED_SOURCES to it.name })
            storageMode?.let { add(STORAGE_MODE to it.value) }
            addAll(attributes.orEmpty().map { ATTRIBUTES to it })

            addAll(files.map { FILES to FileSystemResource(it) })
        }
    return linkedMultiValueMapOf(pairs)
}
