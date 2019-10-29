package ac.uk.ebi.biostd.submission.exceptions

import java.util.regex.Pattern

/**
 * Generated when submission is trying to be attached to a nonexistent project.
 */
class InvalidPatternException(pattern: String, expectedPattern: String) : RuntimeException(
    "Invalid accession $pattern, submission pattern in the form $expectedPattern")

/**
 * Generated when submission is trying to be attached to a nonexistent project.
 */
class InvalidAccNoPattern(accNo: String, validPattern: Pattern) : RuntimeException(
    "Invalid accession number '$accNo', number should match the pattern $validPattern")
