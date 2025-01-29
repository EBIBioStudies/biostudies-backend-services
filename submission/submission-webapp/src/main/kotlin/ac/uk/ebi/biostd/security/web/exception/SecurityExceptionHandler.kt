package ac.uk.ebi.biostd.security.web.exception

import ebi.ac.uk.errors.ValidationNode
import ebi.ac.uk.errors.ValidationNodeStatus.ERROR
import ebi.ac.uk.errors.ValidationTree
import ebi.ac.uk.errors.ValidationTreeStatus.FAIL
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
    fun handle(exception: SecurityException): ResponseEntity<ValidationTree> =
        when (exception) {
            is LoginException,
            is UserNotFoundByEmailException,
            is UserNotFoundByTokenException,
            -> unauthorized(exception)

            is ActKeyNotFoundException,
            is UserPendingRegistrationException,
            is UserWithActivationKeyNotFoundException,
            is UserAlreadyRegister,
            -> badRequest(exception)

            is UnauthorizedOperation -> unauthorized(exception)
            is InvalidSseConfiguration -> badRequest(exception)
            is InvalidCaptchaException -> badRequest(exception)
        }

    private fun unauthorized(exc: SecurityException) = ResponseEntity.status(UNAUTHORIZED).body(asValidationTree(exc))

    private fun badRequest(exc: SecurityException) = ResponseEntity.status(BAD_REQUEST).body(asValidationTree(exc))

    private fun asValidationTree(exc: SecurityException) = ValidationTree(FAIL, ValidationNode(ERROR, exc.message))
}
