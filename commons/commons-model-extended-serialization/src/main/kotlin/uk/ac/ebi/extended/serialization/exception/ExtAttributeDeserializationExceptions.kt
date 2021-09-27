package uk.ac.ebi.extended.serialization.exception

class ExtAttributeNameRequiredException : RuntimeException("The attribute name is required")

class ExtAttributeValueRequiredException(
    name: String
) : RuntimeException("The value for the attribute '$name' is required")
