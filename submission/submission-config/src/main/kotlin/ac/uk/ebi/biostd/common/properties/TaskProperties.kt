package ac.uk.ebi.biostd.common.properties

import ebi.ac.uk.model.SubmissionId
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties
data class TaskProperties(
    val submissions: List<SubmissionId> = emptyList(),
    private val mode: String,
) {
    val taskMode: Mode
        get() = Mode.valueOf(mode)
}

enum class Mode {
    HANDLE_REQUEST,
    CALCULATE_ALL_STATS,
}
