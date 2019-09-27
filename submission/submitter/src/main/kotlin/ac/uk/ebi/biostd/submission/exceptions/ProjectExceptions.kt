package ac.uk.ebi.biostd.submission.exceptions

class InvalidProjectException(project: String) : RuntimeException("The project $project doesn't exist")

class ProjectAlreadyExistingException(project: String) : RuntimeException("The project $project already exists")

class ProjectAccessTagAlreadyExistingException(
    project: String
) : RuntimeException("The access tag with name $project already exists")

class MissingProjectAccessTagException(
    project: String
) : RuntimeException("The project $project doesn't have an access tag")
