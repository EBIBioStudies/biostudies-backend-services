package ac.uk.ebi.biostd.submission.exceptions

class CollectionInvalidAccNoPatternException(message: String) : RuntimeException(message)

class CollectionAlreadyExistingException(collection: String
) : RuntimeException("The collection '$collection' already exists")

class CollectionAccNoTemplateAlreadyExistsException(
    pattern: String
) : RuntimeException("There is a collection already using the accNo template '$pattern'")

class CollectionInvalidAccessTagException(
    project: String
) : RuntimeException("The collection $project doesn't have a valid access tag")
