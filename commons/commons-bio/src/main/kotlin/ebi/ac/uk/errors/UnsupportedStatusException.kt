package ebi.ac.uk.errors

class UnsupportedStatusException(value: String) : RuntimeException("Unsupported status: $value")
