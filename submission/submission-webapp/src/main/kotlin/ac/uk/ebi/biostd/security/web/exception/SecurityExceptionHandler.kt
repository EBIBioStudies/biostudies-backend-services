package ac.uk.ebi.biostd.security.web.exception

import ac.uk.ebi.biostd.security.web.dto.SecurityError
import ebi.ac.uk.security.integration.exception.ActKeyNotFoundException
import ebi.ac.uk.security.integration.exception.InvalidSseConfiguration
import ebi.ac.uk.security.integration.exception.InvalidUserEmailException
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.SecurityException
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import ebi.ac.uk.security.integration.exception.UserAlreadyRegistered
import ebi.ac.uk.security.integration.exception.UserNotFoundByEmailException
import ebi.ac.uk.security.integration.exception.UserNotFoundByTokenException
import ebi.ac.uk.security.integration.exception.UserPendingRegistrationException
import ebi.ac.uk.security.integration.exception.UserWithActivationKeyNotFoundException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class SecurityExceptionHandler {
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(value = [SecurityException::class])
    fun handle(exception: SecurityException): ResponseEntity<SecurityError> {
        return when (exception) {
            is LoginException,
            is UserNotFoundByTokenException -> unauthorized(SecurityError(exception.message))
            is ActKeyNotFoundException,
            is UserNotFoundByEmailException,
            is UserPendingRegistrationException,
            is UserWithActivationKeyNotFoundException,
            is InvalidUserEmailException -> badRequest(SecurityError(exception.message))
            is UserAlreadyRegistered -> badRequest(SecurityError(exception.message))
            is UnauthorizedOperation -> unauthorized(SecurityError(exception.message))
            is InvalidSseConfiguration -> badRequest(SecurityError(exception.message))
        }
    }

    fun <T> unauthorized(body: T): ResponseEntity<T> = ResponseEntity.status(UNAUTHORIZED).body(body)
    fun <T> badRequest(body: T): ResponseEntity<T> = ResponseEntity.status(BAD_REQUEST).body(body)
}
