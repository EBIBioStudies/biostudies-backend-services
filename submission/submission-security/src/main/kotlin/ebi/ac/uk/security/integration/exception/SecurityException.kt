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
    SecurityException("Could not find a user with '$email' and pending activation.")

class UserWithActivationKeyNotFoundException :
    SecurityException("Could not find an un-active user with provided activation key.")

class UserAlreadyRegister(email: String) :
    SecurityException("There is already a user registered with email address '$email'.")
