package ac.uk.ebi.biostd.submission.exceptions

import java.util.regex.Pattern

/**
 * Generated when submission is trying to be attached to not existing project.
 */
class InvalidPatternException(
    private val pattern: String,
    private val expectedPattern: String
) : RuntimeException() {

    override val message: String
        get() = "Invalid accession $pattern, submission pattern in the form $expectedPattern"
}

/**
 * Generated when submission is trying to be attached to not existing project.
 */
class InvalidAccNoPattern(
    private val accNo: String,
    private val validPattern: Pattern
) : RuntimeException() {

    override val message: String
        get() = "Invalid accession number '$accNo', number should match the pattern $validPattern}"
}
