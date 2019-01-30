package ebi.ac.uk.security.exception

sealed class SecurityException(override val message: String) : RuntimeException(message)
class UserNotFoundException(login: String) : SecurityException("Could find an user register with email or login $login")
