package ac.uk.ebi.biostd.persistence.common.exception

class CollectionNotFoundException(
    collection: String,
) : RuntimeException("The collection '$collection' was not found")

class CollectionWithoutPatternException(
    collection: String,
) : RuntimeException("The collection '$collection' does not have a valid accession pattern")

class CollectionValidationException(
    errors: List<String>,
) : RuntimeException("The submission doesn't comply with the collection requirements. Errors: $errors")
