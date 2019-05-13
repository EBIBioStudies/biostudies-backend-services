package ebi.ac.uk.security.integration.exception

sealed class SecurityException(override val message: String) : RuntimeException(message)

class LoginException : SecurityException("Invalid user name or password")
class ActKeyNotFoundException : SecurityException("Could not find a user with pending the activation key")
class UserAlreadyRegister(email: String) : SecurityException("There is already a user register with $email")
