package ebi.ac.uk.security.integration.exception

sealed class SecurityException(override val message: String, cause: Exception?) : RuntimeException(message, cause) {
    constructor(message: String) : this(message, null)
}

class LoginException :
    SecurityException("Invalid email address or password.")

class ActKeyNotFoundException :
    SecurityException("Could not find user with pending activation.")

class UserNotFoundByTokenException :
    SecurityException("Could not find an active session for the provided security token.")

class UserNotFoundByEmailException(email: String) :
    SecurityException("Could not find user with the provided email '$email'.")

class UserPendingRegistrationException(email: String) :
    SecurityException("Could not find user with '$email' and pending activation.")

class UserWithActivationKeyNotFoundException :
    SecurityException("Could not find an un-active user with the provided activation key.")

class UserAlreadyRegistered(email: String) :
    SecurityException("There is already a user registered with the email address '$email'.")

class InvalidUserEmailException(email: String) : SecurityException("The provided email '$email' is invalid")

class UnauthorizedOperation(message: String) : SecurityException(message)

class InvalidSseConfiguration(message: String) : SecurityException(message)
