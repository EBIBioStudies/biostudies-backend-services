package ac.uk.ebi.transpiler.exception

const val INVALID_COLUMN_ERROR_MSG = "Base columns must match the template. %s was expected but got %s instead."

class InvalidColumnException(private val expected: String, private val actual: String) : RuntimeException() {
    override val message: String?
        get() = String.format(INVALID_COLUMN_ERROR_MSG, expected, actual)
}
