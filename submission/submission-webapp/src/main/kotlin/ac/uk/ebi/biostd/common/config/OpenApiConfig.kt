package ac.uk.ebi.biostd.common.config

import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.api.OnBehalfParameters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
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
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.method.HandlerMethod

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

private val PUBLIC_SERVERS =
    listOf(
        Server().url("https://www.ebi.ac.uk/biostudies/submissions/api").description("PROD"),
        Server().url("https://wwwdev.ebi.ac.uk/biostudies/submissions/api").description("BETA"),
    )

private val OPENAPI_TAGS =
    listOf(
        Tag().name("Security").description("User registration, authentication and account management."),
        Tag().name("Collections")
            .description("Collections the authenticated user can submit to or attach files under."),
        Tag().name("Submissions")
            .description("Search submissions and retrieve released or user-visible submission content."),
        Tag().name(
            "Synchronous Submission",
        ).description("Submit content and wait for validation and persistence to complete before returning."),
        Tag().name("Asynchronous Submission").description("Submit content for background validation and processing."),
        Tag().name("Submission Drafts").description("Create, list, update and submit submission drafts."),
        Tag().name("Submission Requests")
            .description("Track asynchronous submission processing requests and their validation results."),
        Tag().name(
            "Submission Utilities",
        ).description("Authenticated helper operations for validating, converting, and managing submissions."),
        Tag().name("File Lists").description("Validation helpers for submission file-list metadata before submission."),
        Tag().name("User Files")
            .description("Browse and manage files in the authenticated user's submission workspace."),
        Tag().name("Group Files").description("Browse and manage files in workspaces shared through security groups."),
        Tag().name("Groups")
            .description("Browse and administer security groups used for shared submission workspaces."),
        Tag().name("Permissions").description("Administrative access-control operations for submission permissions."),
        Tag().name("Extended Submissions")
            .description("Query extended submission documents, file lists, and link lists."),
        Tag().name(
            "Extended Submission Operations",
        ).description("Operational actions for extended submissions and backend maintenance workflows."),
        Tag().name("Post Processing")
            .description("Run post-processing tasks that enrich extended submissions after ingest."),
        Tag().name("User Administration")
            .description("Internal user inspection and home-folder maintenance operations."),
        Tag().name("Cluster Operations").description("Internal cluster job submission, status and log operations."),
        Tag().name("Administration")
            .description("Internal maintenance jobs for submission request files and temporary storage."),
        Tag().name("PMC Links")
            .description("Internal processing endpoints for loading Europe PMC links onto submissions."),
        Tag().name("Statistics").description("Submission statistics lookup and ingestion operations."),
        Tag().name("Statistics Reports")
            .description("Read generated public report files for submission counts and storage sizes."),
    )

@Configuration
@Suppress("TooManyFunctions")
internal class OpenApiConfig {

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
    fun authenticatedHandlerCustomizer(): OperationCustomizer =
        OperationCustomizer { operation, handlerMethod ->
            if (handlerMethod.requiresSessionTokenSecurity()) operation.addSessionTokenSecurity()
            if (handlerMethod.hasOnBehalfParameters()) operation.replaceOnBehalfParameters()

            operation
        }

    @Bean
    fun internalApiGroup(
        hideLegacyAliasesCustomizer: OpenApiCustomizer,
        tagOrderCustomizer: OpenApiCustomizer,
        authenticatedHandlerCustomizer: OperationCustomizer,
    ): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("internal")
            .pathsToMatch("/**")
            .addOperationCustomizer(authenticatedHandlerCustomizer)
            .addOpenApiCustomizer(hideLegacyAliasesCustomizer)
            .addOpenApiCustomizer(tagOrderCustomizer)
            .build()

    @Bean
    fun publicApiGroup(
        hideLegacyAliasesCustomizer: OpenApiCustomizer,
        tagOrderCustomizer: OpenApiCustomizer,
        authenticatedHandlerCustomizer: OperationCustomizer,
        publicServersCustomizer: OpenApiCustomizer,
    ): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("public")
            .pathsToMatch(
                "/auth/**",
                "/collections/**",
                "/files/user/**",
                "/files/groups/**",
                "/submissions/**",
            ).addOperationCustomizer(authenticatedHandlerCustomizer)
            .addOpenApiCustomizer(hideLegacyAliasesCustomizer)
            .addOpenApiCustomizer(publicServersCustomizer)
            .addOpenApiCustomizer(tagOrderCustomizer)
            .build()
}

private fun HandlerMethod.requiresSessionTokenSecurity(): Boolean =
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
