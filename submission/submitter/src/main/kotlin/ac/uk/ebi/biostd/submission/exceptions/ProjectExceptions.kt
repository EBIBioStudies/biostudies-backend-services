package ac.uk.ebi.biostd.submission.exceptions

class CollectionAlreadyExistingException(project: String) : RuntimeException("The project '$project' already exists")

class CollectionAccNoTemplateAlreadyExistsException(
    pattern: String
) : RuntimeException("There is a project already using the accNo template '$pattern'")

class CollectionInvalidAccNoPatternException(message: String) : RuntimeException(message)

class CollectionInvalidAccessTagException(
    project: String
) : RuntimeException("The project $project doesn't have a valid access tag")
