package ac.uk.ebi.biostd.submission.exceptions

/**
 * Generated when submission is trying to be attached to not existing project.
 */
class InvalidProjectException(private val project: String) : RuntimeException() {

    override val message: String
        get() = "The project $project doesn't exists"
}
