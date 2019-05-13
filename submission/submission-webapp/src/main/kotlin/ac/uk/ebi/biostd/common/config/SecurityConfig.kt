package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.property.ApplicationProperties
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ebi.ac.uk.security.integration.SecurityModuleConfig
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityService
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
class SecurityConfig(private val securityFilter: ISecurityFilter) : WebSecurityConfigurerAdapter() {

    @Suppress("SpreadOperator")
    override fun configure(http: HttpSecurity) {
        http.csrf()
            .disable()
            .addFilterBefore(securityFilter, BasicAuthenticationFilter::class.java)
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
    fun securityModuleConfig(userRepository: UserDataRepository, groupRepository: UserGroupDataRepository):
        SecurityModuleConfig = SecurityModuleConfig(userRepository, groupRepository, securityProperties)

    @Bean
    fun securityService(securityConfig: SecurityModuleConfig): ISecurityService = securityConfig.securityService()

    @Bean
    fun groupService(securityConfig: SecurityModuleConfig): IGroupService = securityConfig.groupService()

    @Bean
    fun securityFilter(securityConfig: SecurityModuleConfig): ISecurityFilter = securityConfig.securityFilter()
}
