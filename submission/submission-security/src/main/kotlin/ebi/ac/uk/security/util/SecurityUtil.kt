package ebi.ac.uk.security.util

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import arrow.core.Option
import com.fasterxml.jackson.databind.ObjectMapper
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

private const val DEV_KEY = "975dd2ca-58eb-407b-ba0f-858f15f7304d"
private const val BETA_KEY = "9c584ae3-678a-4462-b685-54c37a1bc047"
private const val PROD_KEY = "01ecc118-dbec-4df8-8fe8-f5cd7364b2b7"

private const val DEV_INSTANCE = "http://ves-hx-f2.ebi.ac.uk:8120"
private const val BETA_INSTANCE = "https://wwwdev.ebi.ac.uk"
private const val PROD_INSTANCE = "https://www.ebi.ac.uk"

private val logger = KotlinLogging.logger {}

/**
 * Provides general purposes security utils methods.
 */
internal class SecurityUtil(
    private val jwtParser: JwtParser,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserDataRepository,
    private val tokenHash: String
) {

    fun createToken(user: User): String {
        return Jwts.builder()
                .setSubject(objectMapper.writeValueAsString(
                    TokenPayload(user.id, user.email, user.fullName)))
                .signWith(SignatureAlgorithm.HS512, tokenHash)
                .compact()
    }

    fun fromToken(token: String): Option<User> {
        return if (jwtParser.isSigned(token)) getFromToken(token) else Option.empty()
    }

    fun newKey() = UUID.randomUUID().toString()

    fun checkPassword(passwordDigest: ByteArray, password: String): Boolean {
        val tokenUser = fromToken(password)
        val isValidSuperUser = tokenUser.fold({ false }, { it.superuser })
        val isValidRegularUser = Arrays.equals(this.getPasswordDigest(password), passwordDigest)
        return isValidSuperUser || isValidRegularUser
    }

    fun getPasswordDigest(password: String) = MessageDigest.getInstance("SHA1").digest(password.toByteArray())!!

    private fun getFromToken(token: String): Option<User> {
        var tokenUser = Option.empty<TokenPayload>()
        try {
            val payload = jwtParser.setSigningKey(tokenHash).parseClaimsJws(token).body.subject
            tokenUser = Option.just(objectMapper.readValue(payload, TokenPayload::class.java))
        } catch (exception: SignatureException) {
            logger.error("detected invalid signature token", exception)
        } catch (exception: MalformedJwtException) {
            logger.error("detected invalid signature token", exception)
        }

        return tokenUser.map { userRepository.getOne(it.id) }
    }

    fun getInstanceUrl(instanceKey: String, path: String): String {
        return when (instanceKey) {
            DEV_KEY -> "$DEV_INSTANCE$path"
            BETA_KEY -> "$BETA_INSTANCE$path"
            PROD_KEY -> "$PROD_INSTANCE$path"
            else -> {
                when {
                    isLocalEnvironment(instanceKey) -> return instanceKey + path
                    else -> throw IllegalArgumentException(String.format("invalid instance key '%s'", instanceKey))
                }
            }
        }
    }

    private fun isLocalEnvironment(instanceKey: String) =
        instanceKey.startsWith("http://localhost") || instanceKey.startsWith("https://localhost")
}
