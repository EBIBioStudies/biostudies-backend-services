package ebi.ac.uk.extended.model

fun ExtFile.copyWithAttributes(attributes: List<ExtAttribute>): ExtFile {
    return when (this) {
        is FireFile -> this.copy(attributes = attributes)
        is NfsFile -> this.copy(attributes = attributes)
    }
}
