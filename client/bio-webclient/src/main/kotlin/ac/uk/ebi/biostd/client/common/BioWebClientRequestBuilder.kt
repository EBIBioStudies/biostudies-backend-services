package ac.uk.ebi.biostd.client.common

import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.PREFERRED_SOURCES
import ebi.ac.uk.model.constants.SUBMISSION
import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap

internal fun getMultipartBody(
    filesConfig: SubmissionFilesConfig,
    submission: Any
): LinkedMultiValueMap<String, Any> {
    val (files, fileMode, preferredSources) = filesConfig

    return LinkedMultiValueMap(
        files.map { FILES to FileSystemResource(it) }
            .plus(preferredSources.map { PREFERRED_SOURCES to it.name })
            .plus(SUBMISSION to submission)
            .plus(FILE_MODE to fileMode.name)
            .groupBy({ it.first }, { it.second })
    )
}
