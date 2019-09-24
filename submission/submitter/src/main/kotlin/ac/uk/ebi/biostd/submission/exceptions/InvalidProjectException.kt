package ac.uk.ebi.biostd.submission.exceptions

/**
 * Generated when submission is trying to be attached to a nonexistent project.
 */
class InvalidProjectException(project: String) : RuntimeException("The project $project doesn't exist")

class MissingProjectAccessTagException(
    project: String
) : RuntimeException("The project $project doesn't have an access tag")
