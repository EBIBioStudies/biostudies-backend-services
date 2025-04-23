package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.security.web.exception.SecurityAccessDeniedHandler
import ac.uk.ebi.biostd.security.web.exception.SecurityAuthEntryPoint
import ac.uk.ebi.biostd.submission.config.FilePersistenceConfig
import ac.uk.ebi.biostd.submission.config.SecurityConfig
import ebi.ac.uk.security.integration.components.SecurityFilter
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod.GET
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
@EnableWebSecurity
@Import(value = [SecurityConfig::class, FilePersistenceConfig::class])
class SecurityWebConfig(
    private val securityFilter: SecurityFilter,
    private val accessDeniedHandler: SecurityAccessDeniedHandler,
    private val authEntryPoint: SecurityAuthEntryPoint,
) : WebSecurityConfigurerAdapter() {
    @Suppress("SpreadOperator")
    override fun configure(http: HttpSecurity) {
        http
            .csrf()
            .disable()
            .addFilterBefore(securityFilter, BasicAuthenticationFilter::class.java)
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(GET, "/security/users/extended/**")
            .permitAll()
            .antMatchers(GET, "/submissions/extended/**")
            .permitAll()
            .antMatchers(GET, "/submissions/*")
            .permitAll()
            .antMatchers("/submissions/ftp/*")
            .permitAll()
            .antMatchers("/auth/**")
            .permitAll()
            .antMatchers("/v2/**")
            .permitAll()
            .antMatchers("/webjars/**")
            .permitAll()
            .antMatchers("/actuator/**")
            .permitAll()
            .antMatchers("/fire/**")
            .permitAll()
            .anyRequest()
            .fullyAuthenticated()
            .and()
            .exceptionHandling()
            .accessDeniedHandler(accessDeniedHandler)
            .authenticationEntryPoint(authEntryPoint)
    }
}
