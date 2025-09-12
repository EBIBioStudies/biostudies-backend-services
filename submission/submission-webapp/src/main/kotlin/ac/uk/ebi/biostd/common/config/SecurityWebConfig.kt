package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.security.web.exception.SecurityAccessDeniedHandler
import ac.uk.ebi.biostd.security.web.exception.SecurityAuthEntryPoint
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ac.uk.ebi.biostd.submission.config.SecurityConfig
import ebi.ac.uk.security.integration.components.SecurityFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod.GET
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
@EnableWebSecurity
@Import(value = [SecurityConfig::class, FilePersistenceConfig::class])
class SecurityWebConfig(
    private val securityFilter: SecurityFilter,
    private val accessDeniedHandler: SecurityAccessDeniedHandler,
    private val authEntryPoint: SecurityAuthEntryPoint,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .addFilterBefore(securityFilter, BasicAuthenticationFilter::class.java)
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(GET, "/security/users/extended/**")
                    .permitAll()
                    .requestMatchers(GET, "/submissions/extended/**")
                    .permitAll()
                    .requestMatchers(GET, "/submissions/*")
                    .permitAll()
                    .requestMatchers("/submissions/ftp/*")
                    .permitAll()
                    .requestMatchers("/auth/**")
                    .permitAll()
                    .requestMatchers("/v2/**")
                    .permitAll()
                    .requestMatchers("/webjars/**")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .requestMatchers("/fire/**")
                    .permitAll()
                    .anyRequest()
                    .fullyAuthenticated()
            }.exceptionHandling { exceptions ->
                exceptions
                    .accessDeniedHandler(accessDeniedHandler)
                    .authenticationEntryPoint(authEntryPoint)
            }

        return http.build()
    }
}
