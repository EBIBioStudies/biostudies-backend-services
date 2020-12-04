package ac.uk.ebi.biostd.persistence.exception

class UserNotFoundException(email: String) : RuntimeException("The user with email '$email' could not be found")
