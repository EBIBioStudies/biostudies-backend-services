package ebi.ac.uk.extended.exception

/**
 * Generated when submission is trying to be attached to a nonexistent project.
 */
class InvalidPatternException(
    private val pattern: String,
    private val expectedPattern: String
) : RuntimeException() {

    override val message: String
        get() = "Invalid accession $pattern, submission pattern in the form $expectedPattern"
}
