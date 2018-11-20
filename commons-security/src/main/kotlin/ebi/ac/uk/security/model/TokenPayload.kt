package ebi.ac.uk.security.model

class TokenPayload {

    constructor(id: Long, email: String, fullName: String?, login: String) {
        this.id = id
        this.email = email
        this.fullName = fullName
        this.login = login
    }

    constructor()

    var id: Long = 0
    var email: String? = null
    var fullName: String? = null
    var login: String? = null
}
