package ac.uk.ebi.biostd.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.builders.ResponseMessageBuilder
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun api(): Docket =
        Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("ac.uk.ebi.biostd.submission.web.resources"))
            .paths(PathSelectors.ant("/projects"))
            .build()
            .apiInfo(apiInfo())
            .useDefaultResponseMessages(false)
            .globalResponseMessage(RequestMethod.GET, listOf(unauthorizedResponseMessage()))
            .globalResponseMessage(RequestMethod.POST, listOf(unauthorizedResponseMessage()))


    private fun apiInfo() =
        ApiInfoBuilder()
            .title("BioStudies API")
            .description("Documentation for the BioStudies project API")
            .contact(Contact("BioStudies Team", "http://www.ebi.ac.uk/biostudies", "biostudies@ebi.ac.uk"))
            .license("Apache 2.0")
            .build()

    private fun unauthorizedResponseMessage() =
        ResponseMessageBuilder()
            .code(HttpStatus.UNAUTHORIZED.value())
            .message("Invalid X-Session-Token")
            .build()
}
