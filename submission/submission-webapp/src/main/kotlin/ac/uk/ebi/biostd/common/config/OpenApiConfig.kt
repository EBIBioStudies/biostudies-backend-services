package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.admin.operations.OperationsService
import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.security.domain.service.ExtUserService
import ac.uk.ebi.biostd.security.domain.service.PermissionService
import ac.uk.ebi.biostd.security.domain.service.RevokePermissionService
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.helpers.CollectionService
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.postprocessing.ExtPostProcessingService
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.domain.service.SubmissionRequestDraftService
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionQueryService
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import ac.uk.ebi.biostd.submission.pmc.PmcRemoteLinksLoader
import ac.uk.ebi.biostd.submission.stats.service.SubmissionStatsService
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.resources.ext.mapping.ExtendedFilePageMapper
import ac.uk.ebi.biostd.submission.web.resources.ext.mapping.ExtendedLinkPageMapper
import ac.uk.ebi.biostd.submission.web.resources.ext.mapping.ExtendedSubmissionPageMapper
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.security.integration.components.IGroupService
import ebi.ac.uk.security.integration.components.ISecurityService
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.service.SecurityService
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.objenesis.ObjenesisStd
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.method.HandlerMethod
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.lang.reflect.Proxy

private const val SESSION_TOKEN_SCHEME = "sessionToken"
private const val SESSION_TOKEN_HEADER = "X-Session-Token"
private const val ON_BEHALF_PARAM_DESCRIPTION = "Email of the user the submission is performed on behalf of."
private const val ON_BEHALF_NAME_PARAM_DESCRIPTION = "Name to use if the on-behalf user needs to be registered."
private const val ON_BEHALF_REGISTER_PARAM_DESCRIPTION = "Whether to register an inactive user for the on-behalf email."

/**
 * Legacy alias paths kept as runtime synonyms of the canonical path. Hidden from the spec so the
 * generated documentation lists each operation once (matches the existing hand-written YAML).
 */
private val HIDDEN_ALIAS_PATHS =
    setOf(
        "/auth/signup",
        "/auth/signin",
        "/auth/signout",
        "/auth/check",
    )

private val PUBLIC_PATH_PREFIXES =
    setOf(
        "/auth",
        "/collections",
        "/files",
        "/submissions",
    )

private val PUBLIC_SERVERS =
    listOf(
        Server().url("https://www.ebi.ac.uk/biostudies/submissions/api").description("PROD"),
        Server().url("https://wwwdev.ebi.ac.uk/biostudies/submissions/api").description("BETA"),
    )

private val OPENAPI_TAGS =
    listOf(
        Tag().name("Security").description("User registration, authentication and account management."),
        Tag().name("Collections").description("Collections the authenticated user can submit to or attach files under."),
        Tag().name("Submissions").description("Search submissions and retrieve released or user-visible submission content."),
        Tag().name(
            "Synchronous Submission",
        ).description("Submit content and wait for validation and persistence to complete before returning."),
        Tag().name("Asynchronous Submission").description("Submit content for background validation and processing."),
        Tag().name("Submission Drafts").description("Create, list, update and submit submission drafts."),
        Tag().name("Submission Requests").description("Track asynchronous submission processing requests and their validation results."),
        Tag().name(
            "Submission Utilities",
        ).description("Authenticated helper operations for validating, converting, and managing submissions."),
        Tag().name("File Lists").description("Validation helpers for submission file-list metadata before submission."),
        Tag().name("User Files").description("Browse and manage files in the authenticated user's submission workspace."),
        Tag().name("Group Files").description("Browse and manage files in workspaces shared through security groups."),
        Tag().name("Groups").description("Browse and administer security groups used for shared submission workspaces."),
        Tag().name("Permissions").description("Administrative access-control operations for submission permissions."),
        Tag().name("Extended Submissions").description("Query extended submission documents, file lists, and link lists."),
        Tag().name(
            "Extended Submission Operations",
        ).description("Operational actions for extended submissions and backend maintenance workflows."),
        Tag().name("Post Processing").description("Run post-processing tasks that enrich extended submissions after ingest."),
        Tag().name("User Administration").description("Internal user inspection and home-folder maintenance operations."),
        Tag().name("Cluster Operations").description("Internal cluster job submission, status and log operations."),
        Tag().name("Administration").description("Internal maintenance jobs for submission request files and temporary storage."),
        Tag().name("PMC Links").description("Internal processing endpoints for loading Europe PMC links onto submissions."),
        Tag().name("Statistics").description("Submission statistics lookup and ingestion operations."),
        Tag().name("Statistics Reports").description("Read generated public report files for submission counts and storage sizes."),
    )

private val PERMIT_ALL_PATH_PREFIXES =
    setOf(
        "/actuator",
        "/auth",
        "/docs",
        "/fire",
        "/pmc",
        "/swagger-ui",
        "/v2",
        "/v3/api-docs",
        "/webjars",
    )

private val PERMIT_ALL_EXACT_PATHS =
    setOf(
        "/swagger-ui.html",
        "/v3/api-docs.yaml",
    )

@Configuration
internal class OpenApiConfig {
    @Bean
    @Profile("openapi-gen")
    fun openApiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }

        return http.build()
    }

    @Bean
    fun bioStudiesOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("BioStudies API")
                    .description(
                        "BioStudies submission backend. The `internal` group documents every endpoint " +
                            "(operational tooling, refresh, FTP, releaser). The `public` group is the subset " +
                            "intended for external submitters.",
                    )
                    .version("1.0.0"),
            )
            .addServersItem(Server().url("http://biostudies-prod.ebi.ac.uk:8788").description("PROD"))
            .addServersItem(Server().url("http://biostudies-beta.ebi.ac.uk:8788").description("BETA"))
            .addServersItem(Server().url("http://biostudies-dev.ebi.ac.uk:8788").description("DEV"))
            .tags(OPENAPI_TAGS)
            .components(
                Components().addSecuritySchemes(
                    SESSION_TOKEN_SCHEME,
                    SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .`in`(SecurityScheme.In.HEADER)
                        .name(SESSION_TOKEN_HEADER)
                        .description("Session token issued by `POST /auth/login` as `sessid`."),
                ),
            )

    @Bean
    fun hideLegacyAliasesCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            openApi.paths?.let { paths ->
                HIDDEN_ALIAS_PATHS.forEach { paths.remove(it) }
            }
        }

    @Bean
    fun publicPathsCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            openApi.paths?.entries?.removeIf { (path, _) ->
                PUBLIC_PATH_PREFIXES.none { path == it || path.startsWith("$it/") }
            }
        }

    @Bean
    fun publicServersCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            openApi.servers = PUBLIC_SERVERS
        }

    @Bean
    fun tagOrderCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            val generatedTags = openApi.tags.orEmpty()
            val generatedTagNames = generatedTags.mapTo(linkedSetOf()) { it.name }
            val orderedTags = OPENAPI_TAGS.filter { it.name in generatedTagNames }
            val knownTagNames = OPENAPI_TAGS.map { it.name }
            val unknownTags = generatedTags.filterNot { it.name in knownTagNames }

            openApi.tags = orderedTags + unknownTags
        }

    @Bean
    fun securityWebConfigCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            openApi.paths?.forEach { (path, pathItem) ->
                pathItem.readOperationsMap().forEach { (method, operation) ->
                    if (operation.requiresAuthentication(method, path)) operation.addSessionTokenSecurity()
                }
            }
        }

    @Bean
    fun authenticatedHandlerCustomizer(): OperationCustomizer =
        OperationCustomizer { operation, handlerMethod ->
            if (handlerMethod.requiresAuthenticatedPrincipal()) operation.addSessionTokenSecurity()
            if (handlerMethod.hasOnBehalfParameters()) operation.replaceOnBehalfParameters()
            operation
        }

    @Bean
    fun internalApiGroup(
        hideLegacyAliasesCustomizer: OpenApiCustomizer,
        securityWebConfigCustomizer: OpenApiCustomizer,
        tagOrderCustomizer: OpenApiCustomizer,
        authenticatedHandlerCustomizer: OperationCustomizer,
    ): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("internal")
            .pathsToMatch("/**")
            .addOperationCustomizer(authenticatedHandlerCustomizer)
            .addOpenApiCustomizer(hideLegacyAliasesCustomizer)
            .addOpenApiCustomizer(securityWebConfigCustomizer)
            .addOpenApiCustomizer(tagOrderCustomizer)
            .build()

    @Bean
    fun publicApiGroup(
        hideLegacyAliasesCustomizer: OpenApiCustomizer,
        securityWebConfigCustomizer: OpenApiCustomizer,
        tagOrderCustomizer: OpenApiCustomizer,
        authenticatedHandlerCustomizer: OperationCustomizer,
        publicPathsCustomizer: OpenApiCustomizer,
        publicServersCustomizer: OpenApiCustomizer,
    ): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("public")
            .pathsToMatch(
                "/auth/**",
                "/collections",
                "/files/user/**",
                "/files/groups/**",
                "/submissions/**",
            ).addOperationCustomizer(authenticatedHandlerCustomizer)
            .addOpenApiCustomizer(hideLegacyAliasesCustomizer)
            .addOpenApiCustomizer(securityWebConfigCustomizer)
            .addOpenApiCustomizer(publicPathsCustomizer)
            .addOpenApiCustomizer(publicServersCustomizer)
            .addOpenApiCustomizer(tagOrderCustomizer)
            .build()
}

private fun Operation.requiresAuthentication(
    method: HttpMethod,
    path: String,
): Boolean = isPermitAllPath(method, path).not()

private fun isPermitAllPath(
    method: HttpMethod,
    path: String,
): Boolean =
    path in PERMIT_ALL_EXACT_PATHS ||
        path.startsWith("/swagger-ui/") ||
        path.startsWith("/v3/api-docs/") ||
        PERMIT_ALL_PATH_PREFIXES.any { path == it || path.startsWith("$it/") } ||
        path.startsWith("/submissions/ftp/") ||
        method == HttpMethod.GET && isPermitAllGetPath(path)

private fun isPermitAllGetPath(path: String): Boolean =
    path.startsWith("/security/users/extended/") ||
        path.startsWith("/submissions/extended/") ||
        path.startsWith("/stats/report/") ||
        path.hasOneSegmentAfter("/submissions")

private fun pathPrefix(path: String): String = if (path.endsWith("/")) path else "$path/"

private fun String.hasOneSegmentAfter(prefix: String): Boolean {
    val normalizedPrefix = pathPrefix(prefix)
    if (startsWith(normalizedPrefix).not()) return false

    val remaining = removePrefix(normalizedPrefix)
    return remaining.isNotBlank() && "/" !in remaining
}

private fun HandlerMethod.requiresAuthenticatedPrincipal(): Boolean =
    AnnotatedElementUtils.hasAnnotation(method, PreAuthorize::class.java) ||
        AnnotatedElementUtils.hasAnnotation(beanType, PreAuthorize::class.java) ||
        methodParameters.any { it.hasParameterAnnotation(BioUser::class.java) } ||
        methodParameters.any { Authentication::class.java.isAssignableFrom(it.parameterType) }

private fun HandlerMethod.hasOnBehalfParameters(): Boolean = methodParameters.any { OnBehalfParameters::class.java == it.parameterType }

private fun Operation.replaceOnBehalfParameters(): Operation {
    parameters = parameters.orEmpty().filterNot { it.isOnBehalfResolverParameter() }.toMutableList()
    addQueryParameter(OnBehalfParameters.ON_BEHALF_PARAM, ON_BEHALF_PARAM_DESCRIPTION, StringSchema())
    addQueryParameter(OnBehalfParameters.USER_NAME_PARAM, ON_BEHALF_NAME_PARAM_DESCRIPTION, StringSchema())
    addQueryParameter(OnBehalfParameters.REGISTER_PARAM, ON_BEHALF_REGISTER_PARAM_DESCRIPTION, BooleanSchema())
    return this
}

private fun Parameter.isOnBehalfResolverParameter(): Boolean =
    name == "onBehalfRequest" ||
        schema?.`$ref`?.endsWith("/OnBehalfParameters").orFalse()

private fun Operation.addQueryParameter(
    name: String,
    description: String,
    schema: io.swagger.v3.oas.models.media.Schema<*>,
) {
    if (parameters.orEmpty().any { it.name == name && it.`in` == "query" }) return

    addParametersItem(
        Parameter()
            .name(name)
            .`in`("query")
            .required(false)
            .description(description)
            .schema(schema),
    )
}

private fun Operation.addSessionTokenSecurity(): Operation {
    val hasSessionToken = security?.any { SESSION_TOKEN_SCHEME in it.keys }.orFalse()
    if (hasSessionToken.not()) addSecurityItem(SecurityRequirement().addList(SESSION_TOKEN_SCHEME))
    return this
}

private fun Boolean?.orFalse(): Boolean = this ?: false

@Configuration
@Profile("openapi-gen")
internal class OpenApiStubsConfig {
    private val objenesis = ObjenesisStd()

    @Bean
    @Primary
    fun openApiSecurityService(): ISecurityService = proxy()

    @Bean
    fun openApiSecurityServiceImpl(): SecurityService = emptyInstance()

    @Bean
    @Primary
    fun openApiSecurityQueryService(): SecurityQueryService = proxy()

    @Bean
    @Primary
    fun openApiGroupService(): IGroupService = proxy()

    @Bean
    @Primary
    fun openApiClusterClient(): ClusterClient = proxy()

    @Bean
    @Primary
    fun openApiFilesMapper(): FilesMapper = emptyInstance()

    @Bean
    @Primary
    fun openApiFileServiceFactory(): FileServiceFactory = emptyInstance()

    @Bean
    @Primary
    fun openApiCollectionService(): CollectionService = emptyInstance()

    @Bean
    @Primary
    fun openApiOnBehalfUtils(): OnBehalfUtils = emptyInstance()

    @Bean
    @Primary
    fun openApiFileListValidator(): FileListValidator = emptyInstance()

    @Bean
    @Primary
    fun openApiSubmissionQueryService(): SubmissionQueryService = emptyInstance()

    @Bean
    @Primary
    fun openApiSubmissionsWebHandler(): SubmissionsWebHandler = emptyInstance()

    @Bean
    @Primary
    fun openApiSubmitWebHandler(): SubmitWebHandler = emptyInstance()

    @Bean
    @Primary
    fun openApiSubmitRequestBuilder(): SubmitRequestBuilder = emptyInstance()

    @Bean
    @Primary
    fun openApiSubmissionRequestDraftService(): SubmissionRequestDraftService = emptyInstance()

    @Bean
    @Primary
    fun openApiSubmissionRequestPersistenceService(): SubmissionRequestPersistenceService = proxy()

    @Bean
    @Primary
    fun openApiSubmissionRequestReleaser(): SubmissionRequestReleaser = emptyInstance()

    @Bean
    @Primary
    fun openApiSerializationService(): SerializationService = proxy()

    @Bean
    @Primary
    fun openApiTempFileGenerator(): TempFileGenerator = emptyInstance()

    @Bean
    @Primary
    fun openApiOperationsService(): OperationsService = emptyInstance()

    @Bean
    @Primary
    fun openApiPermissionService(): PermissionService = emptyInstance()

    @Bean
    @Primary
    fun openApiRevokePermissionService(): RevokePermissionService = emptyInstance()

    @Bean
    @Primary
    fun openApiExtUserService(): ExtUserService = emptyInstance()

    @Bean
    @Primary
    fun openApiExtendedFilePageMapper(): ExtendedFilePageMapper = emptyInstance()

    @Bean
    @Primary
    fun openApiExtendedLinkPageMapper(): ExtendedLinkPageMapper = emptyInstance()

    @Bean
    @Primary
    fun openApiExtendedSubmissionPageMapper(): ExtendedSubmissionPageMapper = emptyInstance()

    @Bean
    @Primary
    fun openApiExtSubmissionQueryService(): ExtSubmissionQueryService = emptyInstance()

    @Bean
    @Primary
    fun openApiExtSubmissionService(): ExtSubmissionService = emptyInstance()

    @Bean
    @Primary
    fun openApiExtPostProcessingService(): ExtPostProcessingService = emptyInstance()

    @Bean
    @Primary
    fun openApiPmcLinksProcessor(): PmcLinksProcessor = emptyInstance()

    @Bean
    @Primary
    fun openApiPmcRemoteLinksLoader(): PmcRemoteLinksLoader = emptyInstance()

    @Bean
    @Primary
    fun openApiSubmissionStatsService(): SubmissionStatsService = emptyInstance()

    @Bean
    @Primary
    fun openApiExtSerializationService(): ExtSerializationService = emptyInstance()

    private inline fun <reified T : Any> proxy(): T =
        Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "OpenAPI generation stub for ${T::class.java.name}"
                "hashCode" -> System.identityHashCode(this)
                "equals" -> false
                else -> throw UnsupportedOperationException("OpenAPI generation stubs must not be invoked")
            }
        } as T

    private inline fun <reified T : Any> emptyInstance(): T = objenesis.newInstance(T::class.java)
}
