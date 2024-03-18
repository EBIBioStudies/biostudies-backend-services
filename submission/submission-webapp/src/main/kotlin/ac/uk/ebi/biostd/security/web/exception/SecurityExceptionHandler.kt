package ac.uk.ebi.biostd.security.web.exception

import ac.uk.ebi.biostd.security.web.dto.SecurityError
import ebi.ac.uk.security.integration.exception.ActKeyNotFoundException
import ebi.ac.uk.security.integration.exception.InvalidCaptchaException
import ebi.ac.uk.security.integration.exception.InvalidSseConfiguration
import ebi.ac.uk.security.integration.exception.LoginException
import ebi.ac.uk.security.integration.exception.SecurityException
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
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

@ControllerAdvice
class SecurityExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = [SecurityException::class])
    fun handle(exception: SecurityException): ResponseEntity<SecurityError> {
        return when (exception) {
            is LoginException,
            is UserNotFoundByTokenException,
            -> unauthorized(SecurityError(exception.message))

            is ActKeyNotFoundException,
            is UserNotFoundByEmailException,
            is UserPendingRegistrationException,
            is UserWithActivationKeyNotFoundException,
            is UserAlreadyRegister,
            -> badRequest(SecurityError(exception.message))

            is UnauthorizedOperation -> unauthorized(SecurityError(exception.message))
            is InvalidSseConfiguration -> badRequest(SecurityError(exception.message))
            is InvalidCaptchaException -> badRequest(SecurityError(exception.message))
        }
    }

    fun <T> unauthorized(body: T): ResponseEntity<T> = ResponseEntity.status(UNAUTHORIZED).body(body)
    fun <T> badRequest(body: T): ResponseEntity<T> = ResponseEntity.status(BAD_REQUEST).body(body)
}
