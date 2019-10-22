package ac.uk.ebi.biostd.submission.exceptions
import ebi.ac.uk.errors.ValidationNode
import java.lang.RuntimeException

class ValidationException(message: String, val causes: List<ValidationNode>) :
    RuntimeException(message)
