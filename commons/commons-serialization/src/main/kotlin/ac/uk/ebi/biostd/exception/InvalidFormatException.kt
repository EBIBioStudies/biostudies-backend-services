package ac.uk.ebi.biostd.exception

class InvalidFormatException(
    format: String,
) : RuntimeException("Unsupported pagetab format: $format")
