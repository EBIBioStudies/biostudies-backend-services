package ebi.ac.uk.extended.exception

import java.util.regex.Pattern

/**
 * Generated when submission is trying to be attached to a nonexistent project.
 */
class InvalidAccNoPattern(private val accNo: String, private val validPattern: Pattern) : RuntimeException() {

    override val message: String
        get() = "Invalid accession number '$accNo', number should match the pattern $validPattern}"
}
