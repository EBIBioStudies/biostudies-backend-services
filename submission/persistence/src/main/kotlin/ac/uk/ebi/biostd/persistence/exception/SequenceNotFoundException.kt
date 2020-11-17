package ac.uk.ebi.biostd.persistence.exception

class SequenceNotFoundException(
    pattern: String
) : RuntimeException("A sequence for the pattern '$pattern' could not be found")
