package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.commons.http.JacksonFactory
import ebi.ac.uk.security.service.GroupService
import ebi.ac.uk.security.service.SecurityService
import ebi.ac.uk.security.util.PasswordVerifier
import ebi.ac.uk.security.util.TokenUtil
import ebi.ac.uk.security.web.SecurityFilter
import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
@EnableWebSecurity
@Import(SecurityBeansConfig::class)
class SecurityConfig(
    private val props: ApplicationProperties,
    private val tokenUtil: TokenUtil
) : WebSecurityConfigurerAdapter() {

    @Suppress("SpreadOperator")
    override fun configure(http: HttpSecurity) {
        http.csrf()
            .disable()
            .addFilterBefore(SecurityFilter(props.environment, tokenUtil), BasicAuthenticationFilter::class.java)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(
                "/auth/login",
                "/auth/signin",
                "/auth/register",
                "/auth/signup").permitAll()
            .anyRequest().fullyAuthenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
    }
}

@Configuration
class SecurityBeansConfig(properties: ApplicationProperties) {

    private val securityProperties = properties.security

    @Bean
    fun tokenUtil(userRepository: UserDataRepository) =
        TokenUtil(jwtParser(), objectMapper(), userRepository, securityProperties.tokenHash)

    @Bean
    fun passwordVerifier(tokenUtil: TokenUtil) = PasswordVerifier(tokenUtil)

    @Bean
    fun securityService(userRepository: UserDataRepository, passwordVerifier: PasswordVerifier, tokenUtil: TokenUtil) =
        SecurityService(userRepository, passwordVerifier, tokenUtil, securityProperties.requireActivation)

    @Bean
    fun objectMapper() = JacksonFactory.createMapper()

    @Bean
    fun jwtParser() = Jwts.parser()!!

    @Bean
    fun groupService(userRepository: UserDataRepository, groupRepository: UserGroupDataRepository) =
        GroupService(groupRepository, userRepository)
}
