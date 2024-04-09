package ac.uk.ebi.biostd.submission.exceptions

/**
 * Generated when submission is trying to be attached to a nonexistent project.
 */
class InvalidPatternException(pattern: String, expectedPattern: String) : RuntimeException(
    "Invalid accession $pattern, submission pattern in the form $expectedPattern",
)
