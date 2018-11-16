package ebi.ac.uk.security.service

import ac.uk.ebi.biostd.persistence.model.User
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import arrow.core.Option
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.security.model.TokenPayload
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SecurityService(
    private val jwtParser: JwtParser,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserDataRepository,
    private val tokenHash: String
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
}
