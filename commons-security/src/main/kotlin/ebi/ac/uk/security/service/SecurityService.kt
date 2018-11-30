package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import arrow.core.Option
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.security.integration.model.SignUpRequest
import ebi.ac.uk.security.model.TokenPayload
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import mu.KotlinLogging
import java.security.MessageDigest
import java.util.Arrays
import java.util.UUID

private val logger = KotlinLogging.logger {}

class SecurityService(
    private val jwtParser: JwtParser,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserDataRepository,
    private val tokenHash: String,
    private val sha1: MessageDigest = MessageDigest.getInstance("SHA1")
) {

    fun fromToken(token: String): Option<User> {
        var tokenUser = Option.empty<TokenPayload>()

        try {
            val payload = jwtParser.setSigningKey(tokenHash).parseClaimsJws(token).body.subject
            tokenUser = Option.just(objectMapper.readValue(payload, TokenPayload::class.java))
        } catch (exception: SignatureException) {
            logger.error("detected invalid signature token")
        } catch (exception: MalformedJwtException) {
            logger.error("detected invalid signature token")
        }

        return tokenUser.map { userRepository.getOne(it.id) }
    }

    fun login(login: String, password: String): String {
        val user = userRepository.findByLoginOrEmail(login, login)

        if (!user.isPresent) {
            throw SecurityException("Could find an user register with email or login '$login'")
        }

        if (!checkPassword(user.get().passwordDigest, password)) {
            throw SecurityException("Given password do not match for user '$login'")
        }

        return createToken(user.get())
    }

    fun registerUser(signUpRequest: SignUpRequest): User {
        val user = User(signUpRequest.email, signUpRequest.password, UUID.randomUUID().toString())
        user.email = signUpRequest.email
        user.login = signUpRequest.login
        user.passwordDigest = sha1.digest(signUpRequest.password.toByteArray())

        return userRepository.save(user)
    }

    private fun createToken(user: User): String {
        return Jwts.builder()
            .setSubject(objectMapper.writeValueAsString(TokenPayload(user.id, user.email, user.fullName, user.login)))
            .signWith(SignatureAlgorithm.HS512, tokenHash)
            .compact()
    }

    private fun checkPassword(passwordDigest: ByteArray, password: String): Boolean {
        val tokenUser = fromToken(password)
        val isValidSuperUser = tokenUser.isDefined() && tokenUser.get().superuser
        val isValidRegularUser = Arrays.equals(getPasswordDigest(password), passwordDigest)

        return isValidSuperUser || isValidRegularUser
    }

    private fun getPasswordDigest(password: String): ByteArray {
        return sha1.digest(password.toByteArray())
    }
}
