package ac.uk.ebi.biostd.client.common

import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ebi.ac.uk.api.STORAGE_MODE
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.PREFERRED_SOURCES
import ebi.ac.uk.model.constants.SUBMISSION
import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap

internal fun getMultipartBody(
    filesConfig: SubmissionFilesConfig,
    submission: Any,
): LinkedMultiValueMap<String, Any> {
    val (files, storageMode, preferredSources) = filesConfig

    return LinkedMultiValueMap(
        files.map { FILES to FileSystemResource(it) }
            .plus(preferredSources.map { PREFERRED_SOURCES to it.name })
            .plus(STORAGE_MODE to storageMode.value)
            .plus(SUBMISSION to submission)
            .groupBy({ it.first }, { it.second })
    )
}
