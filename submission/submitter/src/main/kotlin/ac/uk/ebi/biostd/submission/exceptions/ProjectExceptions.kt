package ac.uk.ebi.biostd.submission.exceptions

class InvalidProjectAccNoException : RuntimeException("AccNo is required for projects")

class ProjectAlreadyExistingException(project: String) : RuntimeException("The project $project already exists")

class ProjectInvalidAccNoPatternException(message: String) : RuntimeException(message)
