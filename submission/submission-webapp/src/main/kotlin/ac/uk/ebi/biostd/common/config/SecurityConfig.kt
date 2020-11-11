package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.TokenDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserGroupDataRepository
import ac.uk.ebi.biostd.persistence.service.UserSqlPermissionsService
import ac.uk.ebi.biostd.security.domain.service.ExtUserService
import ac.uk.ebi.biostd.security.web.SecurityMapper
import ac.uk.ebi.biostd.security.web.exception.SecurityAccessDeniedHandler
import ac.uk.ebi.biostd.security.web.exception.SecurityAuthEntryPoint
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.security.integration.SecurityModuleConfig
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityFilter
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod.GET
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import uk.ac.ebi.events.service.EventsPublisherService

@Configuration
@EnableWebSecurity
@Import(value = [SecurityBeansConfig::class, PersistenceConfig::class])
class SecurityConfig(
    private val securityFilter: ISecurityFilter,
    private val accessDeniedHandler: SecurityAccessDeniedHandler,
    private val authEntryPoint: SecurityAuthEntryPoint
) : WebSecurityConfigurerAdapter() {
    @Suppress("SpreadOperator")
    override fun configure(http: HttpSecurity) {
        http.csrf()
            .disable()
            .addFilterBefore(securityFilter, BasicAuthenticationFilter::class.java)
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers(GET, "/security/users/extended/**").permitAll()
            .antMatchers(GET, "/submissions/extended/*").permitAll()
            .antMatchers(GET, "/submissions/*").permitAll()
            .antMatchers("/auth/**").permitAll()
            .antMatchers("/v2/**").permitAll()
            .antMatchers("/webjars/**").permitAll()
            .antMatchers("/swagger**").permitAll()
            .antMatchers("/swagger-resources/**").permitAll()
            .antMatchers("/actuator/**").permitAll()
            .antMatchers("/fire/**").permitAll()
            .anyRequest().fullyAuthenticated()
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
            .authenticationEntryPoint(authEntryPoint)
    }
}

@Configuration
@Import(PersistenceConfig::class)
@Suppress("TooManyFunctions")
class SecurityBeansConfig(private val objectMapper: ObjectMapper, properties: ApplicationProperties) {
    private val securityProps = properties.security

    @Bean
    fun securityMapper() = SecurityMapper()

    @Bean
    fun accessDeniedHandler(): SecurityAccessDeniedHandler = SecurityAccessDeniedHandler(objectMapper)

    @Bean
    fun securityAuthenticationHandler(): SecurityAuthEntryPoint = SecurityAuthEntryPoint(objectMapper)

    @Bean
    @SuppressWarnings("LongParameterList")
    fun securityModuleConfig(
        userDataRepository: UserDataRepository,
        queryService: SubmissionQueryService,
        tokenRepository: TokenDataRepository,
        tagsRepository: AccessTagDataRepo,
        groupRepository: UserGroupDataRepository,
        userPermissionsService: UserSqlPermissionsService,
        eventsPublisherService: EventsPublisherService
    ): SecurityModuleConfig = SecurityModuleConfig(
        userDataRepository,
        tokenRepository,
        tagsRepository,
        groupRepository,
        queryService,
        userPermissionsService,
        eventsPublisherService,
        securityProps)

    @Bean
    fun securityService(securityConfig: SecurityModuleConfig): ISecurityService = securityConfig.securityService()

    @Bean
    fun groupService(securityConfig: SecurityModuleConfig): IGroupService = securityConfig.groupService()

    @Bean
    fun userPrivilegesService(securityConfig: SecurityModuleConfig): IUserPrivilegesService =
        securityConfig.userPrivilegesService()

    @Bean
    fun securityFilter(securityConfig: SecurityModuleConfig): ISecurityFilter = securityConfig.securityFilter()

    @Bean
    fun extUserService(userDataRepository: UserDataRepository): ExtUserService = ExtUserService(userDataRepository)
}
