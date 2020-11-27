package ac.uk.ebi.biostd.persistence.exception

class ProjectNotFoundException(project: String) : RuntimeException("The project '$project' was not found")

class ProjectWithoutPatternException(
    project: String
) : RuntimeException("The project '$project' does not have a valid accession pattern")
