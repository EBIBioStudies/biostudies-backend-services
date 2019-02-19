package ac.uk.ebi.transpiler.exception

const val INVALID_COLUMN_ERROR_MSG = "Base columns must match the template"

class InvalidColumnException : RuntimeException() {
    override val message: String?
        get() = INVALID_COLUMN_ERROR_MSG
}
