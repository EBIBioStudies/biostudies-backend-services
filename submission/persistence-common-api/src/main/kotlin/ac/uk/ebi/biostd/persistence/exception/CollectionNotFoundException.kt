package ac.uk.ebi.biostd.persistence.exception

class CollectionNotFoundException(collection: String
) : RuntimeException("The project '$collection' was not found")

class CollectionWithoutPatternException(collection: String
) : RuntimeException("The project '$collection' does not have a valid accession pattern")
