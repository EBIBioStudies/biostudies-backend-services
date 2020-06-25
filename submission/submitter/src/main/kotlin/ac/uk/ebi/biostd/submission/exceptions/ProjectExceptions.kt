package ac.uk.ebi.biostd.submission.exceptions

class ProjectAlreadyExistingException(project: String) : RuntimeException("The project $project already exists")

class ProjectAccNoTemplateAlreadyExistsException(
    pattern: String
) : RuntimeException("There is a project already using the accNo template $pattern")

class ProjectInvalidAccNoPatternException(message: String) : RuntimeException(message)

class ProjectInvalidAccessTagException(
    project: String
) : RuntimeException("The project $project doesn't have a valid access tag")
