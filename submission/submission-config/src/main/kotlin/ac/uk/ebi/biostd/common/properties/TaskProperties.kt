package ac.uk.ebi.biostd.common.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties
data class TaskProperties(
    private val mode: String,
) {
    val taskMode: Mode
        get() = Mode.valueOf(mode)
}

enum class Mode {
    HANDLE_REQUEST,
    POST_PROCESS_ALL,
    POST_PROCESS_SINGLE,
    POST_PROCESS_STATS,
    POST_PROCESS_INNER_FILES,
    POST_PROCESS_PAGETAB_FILES,
    POST_PROCESS_DOI,
    LOAD_PMC_LINKS,
    NOTIFY_USER_SPACE_CLEAN_UP,
}
