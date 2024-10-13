package ac.uk.ebi.biostd.client.common

import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.api.SubmitParameters.Companion.SILENT_MODE
import ebi.ac.uk.api.SubmitParameters.Companion.SINGLE_JOB_MODE
import ebi.ac.uk.api.SubmitParameters.Companion.STORAGE_MODE
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
): LinkedMultiValueMap<String, Any> =
    MultipartBuilder()
        .add(SUBMISSION, submission)
        .add(STORAGE_MODE, parameters.storageMode)
        .add(SILENT_MODE, parameters.silentMode)
        .add(SINGLE_JOB_MODE, parameters.singleJobMode)
        .addAll(PREFERRED_SOURCES, parameters.preferredSources.map { it.name })
        .addAll(ATTRIBUTES, parameters.attributes)
        .addAll(FILES, files.map { FileSystemResource(it) })
        .build()
