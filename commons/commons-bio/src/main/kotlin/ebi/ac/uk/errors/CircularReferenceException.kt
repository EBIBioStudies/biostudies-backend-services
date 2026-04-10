package ebi.ac.uk.errors

class CircularReferenceException(
    private val references: List<Pair<String, String>>,
) : RuntimeException() {
    override val message: String
        get() =
            buildString {
                append("The following circular references were found:\n")
                references.forEach { (dir, reference) -> append("- Directory '$dir' contains: '$reference'\n") }
            }
}
