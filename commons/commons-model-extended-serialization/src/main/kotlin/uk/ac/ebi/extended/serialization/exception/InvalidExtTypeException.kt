package uk.ac.ebi.extended.serialization.exception

class InvalidExtTypeException(type: String) : RuntimeException("The given ext type '$type' is invalid")
