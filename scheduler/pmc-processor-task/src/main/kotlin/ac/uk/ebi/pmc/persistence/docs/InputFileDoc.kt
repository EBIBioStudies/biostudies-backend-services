package ac.uk.ebi.pmc.persistence.docs

import java.time.Instant

data class InputFileDoc(
    val name: String,
    val loaded: Instant = Instant.now(),
    val status: InputFileStatus
) {

    companion object Fields {
        const val name = "name"
    }
}

enum class InputFileStatus { PROCESSED, FAILED }
