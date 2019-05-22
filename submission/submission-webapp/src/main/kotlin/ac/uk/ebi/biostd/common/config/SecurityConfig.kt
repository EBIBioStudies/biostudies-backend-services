package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ac.uk.ebi.biostd.security.web.SecurityMapper
import ebi.ac.uk.security.integration.SecurityModuleConfig
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.model.events.PasswordReset
import ebi.ac.uk.security.integration.model.events.UserPreRegister
import io.reactivex.Observable
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
@Import(value = [SecurityBeansConfig::class, PersistenceConfig::class])
class SecurityConfig(private val securityFilter: ISecurityFilter) : WebSecurityConfigurerAdapter() {

    @Suppress("SpreadOperator")
    override fun configure(http: HttpSecurity) {
        http.csrf()
            .disable()
            .addFilterBefore(securityFilter, BasicAuthenticationFilter::class.java)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/auth/**").permitAll()
            .anyRequest().fullyAuthenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
    }
}

@Configuration
class SecurityBeansConfig(properties: ApplicationProperties) {

    private val securityProps = properties.security

    @Bean
    fun securityMapper() = SecurityMapper()

    @Bean
    fun securityModuleConfig(
        userRepository: UserDataRepository,
        tokenRepository: TokenDataRepository,
        groupRepository: UserGroupDataRepository
    ): SecurityModuleConfig = SecurityModuleConfig(userRepository, tokenRepository, groupRepository, securityProps)

    @Bean
    fun securityService(securityConfig: SecurityModuleConfig): ISecurityService = securityConfig.securityService()

    @Bean
    fun groupService(securityConfig: SecurityModuleConfig): IGroupService = securityConfig.groupService()

    @Bean
    fun securityFilter(securityConfig: SecurityModuleConfig): ISecurityFilter = securityConfig.securityFilter()

    @Bean
    fun passwordReset(securityConfig: SecurityModuleConfig): Observable<PasswordReset> = securityConfig.passwordReset

    @Bean
    fun preRegister(securityConfig: SecurityModuleConfig): Observable<UserPreRegister> = securityConfig.userPreRegister
}
