package ac.uk.ebi.pmc.persistence.docs

import java.time.Instant

data class InputFileDoc(
    val name: String,
    val loaded: Instant = Instant.now()
) {

    companion object Fields {
        const val name = "name"
    }
}
