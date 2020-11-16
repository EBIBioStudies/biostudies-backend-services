package ac.uk.ebi.biostd.persistence.exception

class ProjectNotFoundException(project: String) : RuntimeException("The project '$project' doesn't exist")
