package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties
data class TaskProperties(
    val accNo: String,
    val version: Int,
    private val mode: String,
) {
    val taskMode: Mode
        get() = Mode.valueOf(mode)
}

enum class Mode {
    INDEX,
    LOAD,
    INDEX_TO_CLEAN,
    VALIDATE,
    CLEAN,
    COPY,
    CHECK_RELEASED,
    SAVE,
    FINALIZE,
    CALC_STATS,
}
