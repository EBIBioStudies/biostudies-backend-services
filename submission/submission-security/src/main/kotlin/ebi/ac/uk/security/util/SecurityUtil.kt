package ebi.ac.uk.security.util

import ac.uk.ebi.biostd.common.properties.InstanceKeys
import ac.uk.ebi.biostd.persistence.model.DbSecurityToken
import ac.uk.ebi.biostd.persistence.model.DbUser
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.security.model.TokenPayload
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import mu.KotlinLogging
import org.springframework.web.util.UriComponentsBuilder
import java.security.MessageDigest
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

internal const val DEV_INSTANCE = "http://ves-hx-f2.ebi.ac.uk:8120"
internal const val BETA_INSTANCE = "https://wwwdev.ebi.ac.uk"
internal const val PROD_INSTANCE = "https://www.ebi.ac.uk"

private val logger = KotlinLogging.logger {}

/**
 * Provides general purposes security utils methods.
 */
@Suppress("TooManyFunctions")
class SecurityUtil(
    private val jwtParser: JwtParser,
    private val objectMapper: ObjectMapper,
    private val tokenRepository: TokenDataRepository,
    private val userRepository: UserDataRepository,
    private val tokenHash: String,
    private val instanceKeys: InstanceKeys,
) {
    fun createToken(user: DbUser): String {
        return Jwts.builder()
            .setSubject(objectMapper.writeValueAsString(TokenPayload(user.id, user.email, user.fullName)))
            .signWith(SignatureAlgorithm.HS512, tokenHash)
            .compact()
    }

    fun fromToken(token: String): DbUser? = if (jwtParser.isSigned(token)) getFromToken(token) else null

    fun newKey() = UUID.randomUUID().toString()

    fun checkPassword(passwordDigest: ByteArray, password: String): Boolean {
        val tokenUser = fromToken(password)
        val isValidSuperUser = tokenUser?.superuser ?: false
        val isValidRegularUser = getPasswordDigest(password).contentEquals(passwordDigest)
        return isValidSuperUser || isValidRegularUser
    }

    fun getPasswordDigest(password: String) = MessageDigest.getInstance("SHA1").digest(password.toByteArray())!!

    fun getActivationUrl(instanceKey: String, path: String, userKey: String): String {
        return when (instanceKey) {
            instanceKeys.dev -> getUrl(DEV_INSTANCE, path, userKey)
            instanceKeys.beta -> getUrl(BETA_INSTANCE, path, userKey)
            instanceKeys.prod -> getUrl(PROD_INSTANCE, path, userKey)
            else -> {
                when {
                    isLocalEnvironment(instanceKey) -> return getUrl(instanceKey, path, userKey)
                    else -> throw IllegalArgumentException("invalid instance key '$instanceKey'")
                }
            }
        }
    }

    private fun getUrl(instance: String, path: String, userKey: String): String =
        UriComponentsBuilder.fromHttpUrl(instance)
            .pathSegment(normalizePath(path))
            .pathSegment(normalizePath(userKey))
            .build()
            .toUriString()

    private fun normalizePath(path: String) = path.trim('/')

    fun checkToken(tokenKey: String): DbUser? {
        val token = tokenRepository.findById(tokenKey)
        return if (token.isPresent) null else fromToken(tokenKey)
    }

    fun invalidateToken(authToken: String) {
        tokenRepository.save(DbSecurityToken(authToken, OffsetDateTime.now(Clock.systemUTC())))
    }

    private fun isLocalEnvironment(instanceKey: String) =
        instanceKey.startsWith("http://localhost") || instanceKey.startsWith("https://localhost")

    private fun getFromToken(token: String): DbUser? {
        val result = runCatching {
            val payload = jwtParser.setSigningKey(tokenHash).parseClaimsJws(token).body.subject
            val tokenUser = objectMapper.readValue(payload, TokenPayload::class.java)
            userRepository.readByEmail(tokenUser.email)
        }
        result.onFailure { logger.error("detected invalid signature token: ${it.message}") }
        return result.getOrNull()
    }
}
