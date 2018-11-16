package ac.uk.ebi.biostd.config

import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.property.ApplicationProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.security.service.SecurityService
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
    private val securityService: SecurityService
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
            .addFilterBefore(SecurityFilter(props.environment, securityService), BasicAuthenticationFilter::class.java)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .anyRequest().fullyAuthenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
    }
}

@Configuration
class SecurityBeansConfig(private val properties: ApplicationProperties) {

    @Bean
    fun securityService(userRepository: UserDataRepository) =
        SecurityService(jwtParser(), objectMapper(), userRepository, properties.tokenHash)

    @Bean
    fun objectMapper() = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Bean
    fun jwtParser() = Jwts.parser()!!
}