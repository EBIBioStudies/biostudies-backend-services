package ac.uk.ebi.biostd.security.web

import ac.uk.ebi.biostd.security.domain.service.ExtUserService
import ebi.ac.uk.extended.model.ExtUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/security/users/extended")
class ExtUserResource(private val extUserService: ExtUserService) {
    @GetMapping("/{email:.*}")
    fun getExtUser(
        @PathVariable email: String,
    ): ExtUser = extUserService.getExtUser(email)
}
