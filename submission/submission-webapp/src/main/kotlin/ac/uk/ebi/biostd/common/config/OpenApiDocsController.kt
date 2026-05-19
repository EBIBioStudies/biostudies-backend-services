package ac.uk.ebi.biostd.common.config

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@Controller
internal class OpenApiDocsController {
    @Operation(hidden = true)
    @GetMapping("/docs/public")
    fun publicDocs(): ResponseEntity<Void> = redirectToSwaggerUi("public")

    @Operation(hidden = true)
    @GetMapping("/docs/internal")
    fun internalDocs(): ResponseEntity<Void> = redirectToSwaggerUi("internal")

    private fun redirectToSwaggerUi(group: String): ResponseEntity<Void> {
        val apiDocsUrl =
            ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/v3/api-docs/{group}")
                .buildAndExpand(group)
                .toUriString()

        val swaggerUiUrl =
            ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/swagger-ui/index.html")
                .queryParam("url", apiDocsUrl)
                .build()
                .encode()
                .toUri()

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(swaggerUiUrl.toString())).build()
    }
}
